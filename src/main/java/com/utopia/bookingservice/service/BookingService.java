package com.utopia.bookingservice.service;

import java.util.List;

import com.utopia.bookingservice.email.EmailSender;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.entity.User;
import com.utopia.bookingservice.repository.BookingRepository;
import com.utopia.bookingservice.repository.UserRepository;

import org.springframework.data.domain.Page;
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
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        try {
            return bookingRepository.findByUsername(username, pageable);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Could not find booking with username: " + username);
        }
    }

    public Booking create(String username, Booking bookingToCreate) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found with username: " + username));
        bookingToCreate.setUser(user);

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
