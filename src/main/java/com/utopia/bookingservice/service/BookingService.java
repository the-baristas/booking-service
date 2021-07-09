package com.utopia.bookingservice.service;

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

    public Booking update(Long id, Booking targetBooking) {
        bookingRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find booking with id: " + id));
        targetBooking.setId(id);
        try {
            return bookingRepository.save(targetBooking);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not update booking with id: "
                            + targetBooking.getId(),
                    e);
        }
    }

    public void deleteById(Long id) throws ResponseStatusException {
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
