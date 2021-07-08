package com.utopia.bookingservice.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utopia.bookingservice.entity.Airport;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Discount;
import com.utopia.bookingservice.entity.Flight;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.entity.Route;
import com.utopia.bookingservice.repository.BookingRepository;
import com.utopia.bookingservice.repository.DiscountRepository;
import com.utopia.bookingservice.repository.FlightRepository;
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

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PassengerService passengerService;

    @Mock
    private ModelMapper modelMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void findAll_PassengersFound() {
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
        originAirport.setAirportCode("JFK");
        originAirport.setCity("New York");
        originAirport.setActive(Boolean.TRUE);
        route.setOriginAirport(originAirport);
        flight.setRoute(route);
        Page<Passenger> passengersPage = new PageImpl<Passenger>(
                Arrays.asList(passenger));
        when(passengerRepository.findAll(PageRequest.of(0, 1)))
                .thenReturn(passengersPage);

        Page<Passenger> foundPassengersPage = passengerService.findAll(0, 1);
        assertThat(foundPassengersPage, is(passengersPage));
    }

    @Test
    public void findById_ValidId_PassengerFound() {
        Long id = 1L;
        Optional<Passenger> foundPassengerOptional = Optional
                .of(new Passenger());
        when(passengerRepository.findById(id))
                .thenReturn(foundPassengerOptional);

        Passenger returnedPassenger = passengerService.findById(id);
        assertThat(returnedPassenger, is(foundPassengerOptional.get()));
    }

    @Test
    public void findByConfirmationCodeOrUsernameContaining_ValidSearchTerm_PassengersFound() {
        Passenger passenger = new Passenger();
        passenger.setId(1L);
        Page<Passenger> passengersPage = new PageImpl<Passenger>(
                Arrays.asList(passenger));
        String searchTerm = "a";
        Integer pageIndex = 0;
        Integer pageSize = 1;
        when(passengerRepository.findByConfirmationCodeOrUsernameContaining(
                searchTerm, PageRequest.of(pageIndex, pageSize)))
                        .thenReturn(passengersPage);

        Page<Passenger> foundPassengersPage = passengerService
                .findByConfirmationCodeOrUsernameContaining(searchTerm,
                        pageIndex, pageSize);
        assertThat(foundPassengersPage, is(passengersPage));
    }

    @Test
    public void findDistinctByConfirmationCodeOrUsernameContaining_ValidSearchTerm_PassengersFound() {
        Passenger passenger = new Passenger();
        passenger.setId(1L);
        Page<Passenger> passengersPage = new PageImpl<Passenger>(
                Arrays.asList(passenger));
        String searchTerm = "a";
        Integer pageIndex = 0;
        Integer pageSize = 1;
        when(passengerRepository
                .findDistinctByConfirmationCodeOrUsernameContaining(searchTerm,
                        PageRequest.of(pageIndex, pageSize)))
                                .thenReturn(passengersPage);

        Page<Passenger> foundPassengersPage = passengerService
                .findDistinctByConfirmationCodeOrUsernameContaining(searchTerm,
                        pageIndex, pageSize);
        assertThat(foundPassengersPage, is(passengersPage));
    }

    @Test
    public void createPassenger_ValidPassenger_PassengerCreated()
            throws JsonMappingException, JsonProcessingException {
        String originAirportCode = "ABC";
        String destinationAirportCode = "BCD";
        String airplaneModel = "model";
        LocalDateTime departureTime = LocalDateTime.of(2022, 1, 1, 1, 0);
        LocalDateTime arrivalTime = LocalDateTime.of(2022, 1, 1, 2, 0);
        Flight flight = new Flight();
        flight.setFirstClassPrice(1.01);
        Optional<Flight> flightOptional = Optional.of(flight);
        when(flightRepository.identifyFlight(originAirportCode,
                destinationAirportCode, airplaneModel, departureTime,
                arrivalTime)).thenReturn(flightOptional);

        Discount discount = new Discount();
        discount.setDiscountRate(0d);
        Optional<Discount> discountOptional = Optional.of(discount);
        when(discountRepository.findByDiscountType("none"))
                .thenReturn(discountOptional);

        Passenger passengerToCreate = new Passenger();
        Booking bookingToFind = new Booking();
        String confirmationCode = "confirmation_code";
        bookingToFind.setConfirmationCode(confirmationCode);
        passengerToCreate.setBooking(bookingToFind);

        Booking foundBooking = new Booking();
        foundBooking.setLayoverCount(0);
        foundBooking.setTotalPrice(0d);
        Optional<Booking> bookingOptional = Optional.of(foundBooking);
        when(bookingRepository.findByConfirmationCode(confirmationCode))
                .thenReturn(bookingOptional);

        // Clone passengerToCreate object.
        Passenger createdPassenger = objectMapper.readValue(
                objectMapper.writeValueAsString(passengerToCreate),
                Passenger.class);
        when(passengerRepository.save(passengerToCreate))
                .thenReturn(createdPassenger);

        String seatClass = "first";
        LocalDate dateOfBirth = LocalDate.of(2000, 1, 1);

        Passenger newPassenger = passengerService.create(passengerToCreate,
                originAirportCode, destinationAirportCode, airplaneModel,
                departureTime, arrivalTime, seatClass, dateOfBirth);
        assertThat(newPassenger, is(createdPassenger));
    }

    @Test
    public void updatePassenger_ValidPassenger_PassengerUpdated() {
        Passenger updatingPassenger = new Passenger();
        Long id = 1L;
        updatingPassenger.setId(id);
        Optional<Passenger> passengerOptional = Optional.of(updatingPassenger);
        when(passengerRepository.findById(id)).thenReturn(passengerOptional);
        when(passengerRepository.save(updatingPassenger))
                .thenReturn(updatingPassenger);

        Passenger updatedPassenger = passengerService.update(id,
                updatingPassenger);
        assertThat(updatedPassenger, is(updatingPassenger));
    }

    @Test
    public void deletePassengerById_ValidId_PassengerDeleted() {
        Passenger passenger = new Passenger();
        Long id = 1L;
        passenger.setId(id);
        Optional<Passenger> passengerOptional = Optional.of(passenger);
        when(passengerRepository.findById(id)).thenReturn(passengerOptional);

        passengerService.deleteById(id);
        verify(passengerRepository, times(1)).deleteById(id);
    }
}
