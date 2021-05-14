package com.utopia.bookingservice.service;

import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.repository.PassengerRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PassengerService {
    private final PassengerRepository passengerRepository;

    public Page<Passenger> findAllPassengers(Integer pageIndex,
            Integer pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return passengerRepository.findAll(pageable);
    }

    public Passenger createPassenger(Passenger passeneger) {
        try {
            return passengerRepository.save(passeneger);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not create passenger with id: " + passeneger.getId(),
                    e);
        }
    }

    public Passenger updatePassenger(Passenger passenger) {
        passengerRepository.findById(passenger.getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find passenger with id="
                                + passenger.getId()));
        return passengerRepository.save(passenger);
    }

    public void deletePassengerById(Long id) {
        passengerRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find passenger with id=" + id));
        passengerRepository.deleteById(id);
    }
}
