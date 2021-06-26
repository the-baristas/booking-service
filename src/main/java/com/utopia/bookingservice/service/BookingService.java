package com.utopia.bookingservice.service;

import java.util.List;

import com.utopia.bookingservice.entity.Booking;
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

    public List<Booking> findAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking findByConfirmationCode(String confirmationCode) {
        return bookingRepository.findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Could not find booking with confirmation code: "
                                + confirmationCode));
    }

    public List<Booking> findByConfirmationCodeContaining(
            String confirmationCode) {
        return bookingRepository
                .findByConfirmationCodeContaining(confirmationCode);
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

    public Booking create(Booking bookingToCreate, String username) {
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

    public Booking update(Long id, String username, Booking targetBooking) {
        bookingRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find booking with id: " + id));
        targetBooking.setId(id);
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found with username: " + username));
        targetBooking.setUser(user);
        try {
            return bookingRepository.save(targetBooking);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not update booking with id: "
                            + targetBooking.getId(),
                    e);
        }
    }

    public void deleteBookingById(Long id) throws ResponseStatusException {
        bookingRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find booking with id = " + id));
        try {
            bookingRepository.deleteById(id);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not delete booking with id: " + id, exception);
        }
    }
}
