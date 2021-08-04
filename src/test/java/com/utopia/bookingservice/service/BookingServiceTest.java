package com.utopia.bookingservice.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Flight;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.entity.User;
import com.utopia.bookingservice.repository.BookingRepository;
import com.utopia.bookingservice.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PassengerService passengerService;

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private ModelMapper modelMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void findAll_BookingsFound() {
        Booking foundBooking = new Booking();
        foundBooking.setId(1L);
        foundBooking.setActive(Boolean.TRUE);
        foundBooking.setConfirmationCode("a");
        foundBooking.setLayoverCount(0);
        foundBooking.setTotalPrice(0.01);
        Page<Booking> foundBookingsPage = new PageImpl<Booking>(
                Arrays.asList(foundBooking));
        Pageable pageable = PageRequest.of(0, 1);
        when(bookingRepository.findAll(pageable)).thenReturn(foundBookingsPage);

        Page<Booking> returnedBookingsPage = bookingService.findAll(0, 1);
        assertThat(returnedBookingsPage, is(foundBookingsPage));
    }

    @Test
    void findByConfirmationCode_ValidConfirmationCode_BookingFound() {
        Booking foundBooking = new Booking();
        foundBooking.setId(1L);
        foundBooking.setActive(Boolean.TRUE);
        String confirmationCode = "a";
        foundBooking.setConfirmationCode(confirmationCode);
        foundBooking.setLayoverCount(0);
        foundBooking.setTotalPrice(0.01);
        when(bookingRepository.findByConfirmationCode(confirmationCode))
                .thenReturn(Optional.of(foundBooking));

        Booking returnedBooking = bookingService
                .findByConfirmationCode(confirmationCode);
        assertThat(returnedBooking, is(foundBooking));
    }

    @Test
    void findByConfirmationCodeContaining_ValidSearchTerm_BookingsPageFound() {
        Booking foundBooking = new Booking();
        foundBooking.setId(1L);
        foundBooking.setActive(Boolean.TRUE);
        String confirmationCode = "a";
        foundBooking.setConfirmationCode(confirmationCode);
        foundBooking.setLayoverCount(0);
        foundBooking.setTotalPrice(0.01);
        String searchTerm = "a";
        Page<Booking> foundBookingsPage = new PageImpl<Booking>(
                Arrays.asList(foundBooking));
        Integer pageIndex = 0;
        Integer pageSize = 1;
        when(bookingRepository.findByConfirmationCodeContaining(searchTerm,
                PageRequest.of(pageIndex, pageSize)))
                        .thenReturn(foundBookingsPage);

        Page<Booking> returnedBookingsPage = bookingService
                .findByConfirmationCodeContaining(searchTerm, pageIndex,
                        pageSize);
        assertThat(returnedBookingsPage, is(foundBookingsPage));
    }

    @Test
    void findByUsername_ValidUsername_BookingFound() {
        String username = "username";
        Page<Booking> foundBookingsPage = new PageImpl<Booking>(
                Arrays.asList(new Booking()));
        Integer pageIndex = 0;
        Integer pageSize = 1;
        when(bookingRepository.findByUsername(username,
                PageRequest.of(pageIndex, pageSize)))
                        .thenReturn(foundBookingsPage);

        Page<Booking> returnedBookingsPage = bookingService
                .findByUsername(username, pageIndex, pageSize);
        assertThat(returnedBookingsPage, is(foundBookingsPage));
    }

    @Test
    void create_ValidBooking_BookingCreated() {
        Booking bookingToCreate = new Booking();
        bookingToCreate.setId(1L);
        bookingToCreate.setActive(Boolean.TRUE);
        bookingToCreate.setConfirmationCode("a");
        bookingToCreate.setLayoverCount(0);
        bookingToCreate.setTotalPrice(0.01);

        Optional<User> userOptional = Optional.of(new User());
        when(userRepository.findByUsername("username"))
                .thenReturn(userOptional);

        Booking createdBooking = new Booking();
        createdBooking.setId(1L);
        createdBooking.setActive(Boolean.TRUE);
        createdBooking.setConfirmationCode("a");
        createdBooking.setLayoverCount(0);
        createdBooking.setTotalPrice(0.01);
        when(bookingRepository.save(bookingToCreate))
                .thenReturn(createdBooking);

        Booking newBooking = bookingService.create("username", bookingToCreate);

        assertThat(newBooking, is(createdBooking));
    }

    @Test
    void update_NoNewValues_BookingUpdated()
            throws JsonMappingException, JsonProcessingException {
        Long id = 1L;
        Booking bookingToUpdate = new Booking();
        when(bookingRepository.findById(id))
                .thenReturn(Optional.of(bookingToUpdate));

        Booking updatedBooking = objectMapper.readValue(
                objectMapper.writeValueAsString(bookingToUpdate),
                Booking.class);
        when(bookingRepository.save(bookingToUpdate))
                .thenReturn(updatedBooking);

        String confirmationCode = "confirmation_code";
        Boolean active = Boolean.TRUE;
        Integer layoverCount = 0;
        Double totalPrice = 1.01;
        Booking newBooking = bookingService.update(id, confirmationCode, active,
                layoverCount, totalPrice);

        assertThat(newBooking, is(updatedBooking));
    }

    @Test
    void update_ActiveFalseChangedFromTrue_BookingUpdated()
            throws JsonMappingException, JsonProcessingException {
        Long id = 1L;
        Booking bookingToUpdate = new Booking();
        bookingToUpdate.setId(id);
        bookingToUpdate.setActive(Boolean.TRUE);
        Passenger passenger1 = new Passenger();
        passenger1.setSeatClass("first");
        passenger1.setFlight(new Flight());
        passenger1.getFlight().setReservedFirstClassSeatsCount(1);
        Passenger passenger2 = new Passenger();
        passenger2.setSeatClass("business");
        passenger2.setFlight(new Flight());
        passenger2.getFlight().setReservedFirstClassSeatsCount(1);
        List<Passenger> passengers = Arrays.asList(passenger1, passenger2);
        bookingToUpdate.setPassengers(passengers);
        when(bookingRepository.findById(id))
                .thenReturn(Optional.of(bookingToUpdate));

        Booking updatedBooking = objectMapper.readValue(
                objectMapper.writeValueAsString(bookingToUpdate),
                Booking.class);
        when(bookingRepository.save(bookingToUpdate))
                .thenReturn(updatedBooking);

        String confirmationCode = "confirmation_code";
        Boolean active = Boolean.FALSE;
        Integer layoverCount = 0;
        Double totalPrice = 1.01;
        Booking newBooking = bookingService.update(id, confirmationCode, active,
                layoverCount, totalPrice);

        assertThat(newBooking, is(updatedBooking));
        verify(passengerService, times(1)).decrementReservedSeatsCount(
                passenger1.getSeatClass(), passenger1.getFlight());
        verify(passengerService, times(1)).decrementReservedSeatsCount(
                passenger2.getSeatClass(), passenger2.getFlight());
    }

    @Test
    void deleteById_ValidId_BookingDeleted() {
        Passenger passenger1 = new Passenger();
        passenger1.setSeatClass("first");
        passenger1.setFlight(new Flight());
        passenger1.getFlight().setReservedFirstClassSeatsCount(0);
        Passenger passenger2 = new Passenger();
        passenger2.setSeatClass("business");
        passenger2.setFlight(new Flight());
        passenger2.getFlight().setReservedFirstClassSeatsCount(0);
        Long id = 1L;
        Booking bookingToDelete = new Booking();
        bookingToDelete.setId(id);
        bookingToDelete.setActive(Boolean.TRUE);
        List<Passenger> passengers = Arrays.asList(passenger1, passenger2);
        bookingToDelete.setPassengers(passengers);
        when(bookingRepository.findById(id))
                .thenReturn(Optional.of(bookingToDelete));

        bookingService.deleteById(id);

        verify(bookingRepository, times(1)).deleteById(id);
        verify(passengerService, times(1)).decrementReservedSeatsCount(
                passenger1.getSeatClass(), passenger1.getFlight());
        verify(passengerService, times(1)).decrementReservedSeatsCount(
                passenger2.getSeatClass(), passenger2.getFlight());
    }
}
