package com.utopia.bookingservice.service;

import java.util.List;

import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Flight;
import com.utopia.bookingservice.repository.BookingRepository;
import com.utopia.bookingservice.repository.FlightRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;

    public List<Booking> findAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking findByConfirmationCode(String confirmationCode) {
        Booking booking = bookingRepository
                .findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Could not find booking with confirmation code: "
                                + confirmationCode));
        List<Flight> flights = flightRepository
                .getBookingFlights(confirmationCode);
        booking.setFlights(flights);
        return booking;
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
                        "Could not find booking with id: " + id));
        booking.setId(id);
        try {
            return bookingRepository.save(booking);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not update booking with id: " + booking.getId(), e);
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
