package com.utopia.bookingservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.stripe.exception.StripeException;
import com.utopia.bookingservice.dto.BookingPurchaseDto;
import com.utopia.bookingservice.email.EmailSender;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.entity.Payment;
import com.utopia.bookingservice.entity.User;
import com.utopia.bookingservice.repository.BookingRepository;
import com.utopia.bookingservice.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PassengerService passengerService;
    private final EmailSender emailSender;
    private final PaymentService paymentService;

    public Page<Booking> findAll(Integer pageIndex, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return bookingRepository.findAll(pageable);
    }

    public Booking findByConfirmationCode(String confirmationCode) {
        return bookingRepository.findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Could not find booking with confirmation code: "
                                + confirmationCode));
    }

    public Page<Booking> findByConfirmationCodeContaining(String searchTerm,
            Integer pageIndex, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return bookingRepository.findByConfirmationCodeContaining(searchTerm,
                pageable);
    }

    public Page<Booking> findByUsername(String username, Integer pageIndex,
            Integer pageSize) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "A user with this username does not exist: " + username));

        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        try {
            return bookingRepository.findByUsername(username, pageable);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Could not find booking with username: " + username);
        }
    }

    public Page<Booking> findByUsername(String username, String searchTerm, Integer pageIndex, Integer pageSize){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "A user with this username does not exist: " + username));

        try {
            //get all the bookings made by this username, then filter it based on the search term.
            //a booking will be returned if any passenger's name in the booking contains the search term
            List<Booking> allFilteredBookings = bookingRepository.findAllByUsername(username)
                    .stream().filter(
                            (booking) -> {
                                 for(Passenger p : booking.getPassengers())
                                     if((p.getGivenName() + p.getFamilyName() ).toLowerCase().contains(searchTerm.toLowerCase()))
                                         return true;
                                 return false;
                            }
                    )
                    .collect(Collectors.toList());

            return new PageImpl<Booking>(allFilteredBookings, PageRequest.of(pageIndex, pageSize), allFilteredBookings.size());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Could not find bookings with username: " + username);
        }
    }

    public Page<Booking> findPendingFlightsByUsername(String username, Integer pageIndex, Integer pageSize){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "A user with this username does not exist: " + username));

        try {

            LocalDateTime now = LocalDateTime.now();
            List<Booking> allPendingBookings = bookingRepository.findAllByUsername(username)
                    .stream().filter(
                            booking -> booking
                                    .findEarliestDepartingFlight()
                                    .getDepartureTime().isAfter(now) && booking.getActive()
                            )
                    .collect(Collectors.toList());

            return new PageImpl<Booking>(allPendingBookings, PageRequest.of(pageIndex, pageSize), allPendingBookings.size());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Could not find bookings with username: " + username);
        }
    }

    public Page<Booking> findPendingFlightsByUsername(String username, String searchTerm, Integer pageIndex, Integer pageSize){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "A user with this username does not exist: " + username));

        try {
            LocalDateTime now = LocalDateTime.now();
            List<Booking> allPendingBookings = bookingRepository.findAllByUsername(username)
                    .stream().filter(
                            booking -> booking
                                    .findEarliestDepartingFlight()
                                    .getDepartureTime().isAfter(now) && booking.getActive()
                    ).filter(
                            (booking) -> {
                                for(Passenger p : booking.getPassengers())
                                    if((p.getGivenName() + p.getFamilyName() ).toLowerCase().contains(searchTerm.toLowerCase()))
                                        return true;
                                return false;
                            }
                    )
                    .collect(Collectors.toList());

            return new PageImpl<Booking>(allPendingBookings, PageRequest.of(pageIndex, pageSize), allPendingBookings.size());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Could not find bookings with username: " + username);
        }
    }

    public Booking create(String username, Booking bookingToCreate) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found with username: " + username));
        bookingToCreate.setUser(user);
        bookingToCreate.setConfirmationCode(UUID.randomUUID().toString());
        bookingToCreate.setTotalPrice(0d);
        try {
            return bookingRepository.save(bookingToCreate);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not create booking with id: "
                            + bookingToCreate.getId(),
                    e);
        }
    }

    @Transactional
    public Booking update(Long id, String confirmationCode, Boolean newActive,
            Integer layoverCount, Double totalPrice) {
        Booking bookingToUpdate = bookingRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find booking with id: " + id));
        Boolean currentActive = bookingToUpdate.getActive();
        if (!currentActive && newActive) {
            incrementReservedSeatsCounts(bookingToUpdate.getPassengers());
        } else if (currentActive && !newActive) {
            decrementReservedSeatsCounts(bookingToUpdate.getPassengers());
        }
        bookingToUpdate.setConfirmationCode(confirmationCode);
        bookingToUpdate.setActive(newActive);
        bookingToUpdate.setLayoverCount(layoverCount);
        bookingToUpdate.setTotalPrice(totalPrice);
        try {
            return bookingRepository.save(bookingToUpdate);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not update booking with id: " + id, e);
        }
    }

    public void deleteById(Long id) throws ResponseStatusException {
        Booking bookingToDelete = bookingRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find booking with id = " + id));
        decrementReservedSeatsCounts(bookingToDelete.getPassengers());
        try {
            bookingRepository.deleteById(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not delete booking with id: " + id, e);
        }
    }

    public void sendEmail(Booking booking) {
        emailSender.sendBookingDetails(booking);
    }

    @Transactional
    public void refundBooking(Long bookingId, Long refundAmount)
            throws StripeException {
        // get booking by id
        Booking booking = bookingRepository.findById(bookingId).get();

        // if the booking is already refunded, do nothing
        if (booking.getPayment().isRefunded())
            return;

        update(bookingId, booking.getConfirmationCode(), false,
                booking.getLayoverCount(), booking.getTotalPrice());

        // call paymentService to refund stripe payment
        paymentService.refundPayment(booking.getPayment(), refundAmount);
    }

    @Transactional
    public Booking purchaseBooking(List<Passenger> passengers, Integer layoverCount, Long totalPrice, String username, String stripeId){
        Booking newBooking = new Booking();
        newBooking.setLayoverCount(layoverCount);
        newBooking = create(username, newBooking);

        for (Passenger passenger : passengers) {
            passenger.setBooking(newBooking);
            passengerService.create(passenger);
        }

        Payment payment = new Payment(newBooking, stripeId, false);

        update(newBooking.getId(), newBooking.getConfirmationCode(), true, layoverCount, totalPrice.doubleValue());
        paymentService.createPayment(payment);
        return newBooking;
    }

    private void incrementReservedSeatsCounts(List<Passenger> passengers) {
        for (Passenger passenger : passengers) {
            passengerService.incrementReservedSeatsCount(
                    passenger.getSeatClass(), passenger.getFlight());
        }
    }

    private void decrementReservedSeatsCounts(List<Passenger> passengers) {
        for (Passenger passenger : passengers) {
            passengerService.decrementReservedSeatsCount(
                    passenger.getSeatClass(), passenger.getFlight());
        }
    }
}
