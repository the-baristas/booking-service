package com.utopia.bookingservice.service;

import java.time.LocalDateTime;

import com.utopia.bookingservice.entity.Flight;
import com.utopia.bookingservice.repository.FlightRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlightService {
    private final FlightRepository flightRepository;

    public Flight identifyFlight(String originAirportCode,
            String destinationAirportCode, String airplaneModel,
            LocalDateTime departureTime, LocalDateTime arrivalTime) {
        return flightRepository
                .identifyFlight(originAirportCode, destinationAirportCode,
                        airplaneModel, departureTime, arrivalTime)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Could not identify flight using " + originAirportCode
                                + ", " + destinationAirportCode + ", "
                                + airplaneModel + ", " + departureTime + ", "
                                + arrivalTime));
    }
}
