package com.utopia.bookingservice.service;

import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Flight;
import com.utopia.bookingservice.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FlightService {

    @Autowired
    FlightRepository flightRepository;

    public Flight findById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Could not find flight with id: "
                                + id));
    }
}
