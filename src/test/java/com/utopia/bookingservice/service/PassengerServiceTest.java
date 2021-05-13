package com.utopia.bookingservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.utopia.bookingservice.dto.PassengerDto;
import com.utopia.bookingservice.repository.PassengerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

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
        PassengerDto passengerDto = new PassengerDto();
        passengerDto.setId(1L);
        passengerDto.setBookingId(1L);
        passengerDto.setBookingActive(Boolean.TRUE);
        passengerDto.setBookingConfirmationCode("A1");
        passengerDto.setLayoverCount(0);
        passengerDto.setBookingTotalPrice(1.11);
        passengerDto.setFlightId(1L);
        passengerDto.setFlightActive(Boolean.TRUE);
        passengerDto.setDepartureTime(
                ZonedDateTime.of(2021, 10, 10, 10, 10, 0, 0, ZoneOffset.UTC));
        passengerDto.setArrivalTime(
                ZonedDateTime.of(2021, 10, 10, 10, 15, 0, 0, ZoneOffset.UTC));
        passengerDto.setRouteId(1L);
        passengerDto.setRouteActive(Boolean.TRUE);
        passengerDto.setOriginAirportCode("JFK");
        passengerDto.setOriginAirportActive(Boolean.TRUE);
        passengerDto.setOriginCity("New York");
        passengerDto.setDestinationAirportCode("LAX");
        passengerDto.setDestinationAirportActive(Boolean.TRUE);
        passengerDto.setDestinationCity("Los Angeles");
        passengerDto.setDiscountType("none");
        passengerDto.setDiscountRate(1.0);
        passengerDto.setGivenName("Givenname");
        passengerDto.setFamilyName("Familyname");
        passengerDto.setDateOfBirth(LocalDate.of(2000, 1, 1));
        passengerDto.setGender("male");
        passengerDto.setAddress("1 Main Street, City, State 10000");
        passengerDto.setSeatNumber(1);
        passengerDto.setCheckInGroup(1);
    }
}
