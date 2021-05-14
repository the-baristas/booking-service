package com.utopia.bookingservice.service;

import java.util.List;

import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.repository.BookingRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;

    public List<Booking> findAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> findByConfirmationCodeContaining(
            String confirmationCode) {
        return bookingRepository
                .findByConfirmationCodeContaining(confirmationCode);
    }

    public Booking createBooking(Booking booking) {
        try {
            return bookingRepository.save(booking);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not create booking with id: " + booking.getId(), e);
        }
    }

    public Booking updateBooking(Long id, Booking booking) {
        bookingRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find airplane with id: " + id));
        booking.setId(id);
        try {
            return bookingRepository.save(booking);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not update airplane with id: " + booking.getId(), e);
        }
    }

    public void deleteBookingById(Long id) throws ResponseStatusException {
        bookingRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find airplane with id = " + id));
        try {
            bookingRepository.deleteById(id);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not delete airplane with id: " + id, exception);
        }
    }
}
