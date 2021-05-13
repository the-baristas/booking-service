package com.utopia.bookingservice.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import com.utopia.bookingservice.entity.Airport;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Flight;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.entity.Route;
import com.utopia.bookingservice.repository.PassengerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
public class PassengerServiceTest {
    @Mock
    private PassengerRepository passengerRepository;

    @InjectMocks
    private PassengerService passengerService;

    @Mock
    ModelMapper modelMapper;

    @Test
    public void findAllPassengers_PassengersFound() {
        Passenger passenger = new Passenger();
        passenger.setId(1L);
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setActive(Boolean.TRUE);
        booking.setConfirmationCode("A1");
        booking.setLayoverCount(0);
        booking.setTotalPrice(1.01);
        passenger.setBooking(booking);
        Flight flight = new Flight();
        flight.setId(1L);
        Route route = new Route();
        route.setId(1L);
        Airport originAirport = new Airport();
        originAirport.setIataId("JFK");
        originAirport.setCity("New York");
        originAirport.setActive(Boolean.TRUE);
        route.setOriginAirport(originAirport);
        flight.setRoute(route);
        Page<Passenger> passengers = new PageImpl<Passenger>(
                Arrays.asList(passenger));
        when(passengerRepository.findAll(PageRequest.of(0, 1)))
                .thenReturn(passengers);

        Page<Passenger> foundPassengers = passengerService.findAllPassengers(0,
                1);
        System.out.println(foundPassengers);
        assertThat(passengers, is(foundPassengers));
    }

    @Test
    public void createPassenger_ValidPassenger_PassengerCreated() {
        Passenger passengerToSave = new Passenger();
        Passenger savedPassenger = new Passenger();
        when(passengerRepository.save(passengerToSave))
                .thenReturn(savedPassenger);

        Passenger newPassenger = passengerService
                .createPassenger(passengerToSave);
        assertThat(newPassenger, is(savedPassenger));
    }
}
