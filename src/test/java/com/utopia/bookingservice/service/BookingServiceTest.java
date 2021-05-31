package com.utopia.bookingservice.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import com.utopia.bookingservice.dto.BookingDto;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.repository.BookingRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private ModelMapper modelMapper;

    @Test
    public void findAllBookings_BookingsFound() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setActive(Boolean.TRUE);
        bookingDto.setConfirmationCode("a");
        bookingDto.setLayoverCount(0);
        bookingDto.setTotalPrice(0.01);
        Booking booking = modelMapper.map(bookingDto, Booking.class);
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findAll()).thenReturn(bookings);

        List<Booking> foundBookings = bookingService.findAllBookings();
        assertThat(bookings, is(foundBookings));
    }

    @Test
    public void createBooking_Booking_BookingSaved() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setActive(Boolean.TRUE);
        bookingDto.setConfirmationCode("a");
        bookingDto.setLayoverCount(0);
        bookingDto.setTotalPrice(0.01);
        Booking booking = modelMapper.map(bookingDto, Booking.class);

        BookingDto savedBookingDto = new BookingDto();
        savedBookingDto.setId(1L);
        savedBookingDto.setActive(Boolean.TRUE);
        savedBookingDto.setConfirmationCode("a");
        savedBookingDto.setLayoverCount(0);
        savedBookingDto.setTotalPrice(0.01);
        Booking savedBooking = modelMapper.map(savedBookingDto, Booking.class);
        when(bookingRepository.save(booking)).thenReturn(savedBooking);

        Booking newBooking = bookingService.create(booking);
        assertThat(newBooking, is(savedBooking));
    }
}
