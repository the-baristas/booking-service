package com.utopia.bookingservice.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utopia.bookingservice.entity.Booking;
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
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private ModelMapper modelMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void findAll_BookingsFound() {
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
    public void findByConfirmationCode_ValidConfirmationCode_BookingFound() {
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
    public void findByConfirmationCodeContaining_ValidSearchTerm_BookingsPageFound() {
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
    public void findByUsername_ValidUsername_BookingFound() {
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
    public void create_ValidBooking_BookingCreated() {
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
    public void update_ValidIdValidBooking_BookingUpdated()
            throws JsonMappingException, JsonProcessingException {
        Long id = 1L;
        Booking targetBooking = new Booking();
        when(bookingRepository.findById(id))
                .thenReturn(Optional.of(new Booking()));

        Booking updatedBooking = objectMapper.readValue(
                objectMapper.writeValueAsString(targetBooking), Booking.class);
        when(bookingRepository.save(targetBooking)).thenReturn(updatedBooking);

        Booking newBooking = bookingService.update(id, targetBooking);

        assertThat(newBooking, is(updatedBooking));
    }

    @Test
    public void deleteById_ValidId_BookingDeleted() {
        Long id = 1L;
        when(bookingRepository.findById(id))
                .thenReturn(Optional.of(new Booking()));

        bookingService.deleteById(id);
        verify(bookingRepository, times(1)).deleteById(id);
    }
}
