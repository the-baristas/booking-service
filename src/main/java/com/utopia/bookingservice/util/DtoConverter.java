package com.utopia.bookingservice.util;

import com.utopia.bookingservice.dto.BookingDto;
import com.utopia.bookingservice.dto.FlightDto;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Flight;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class DtoConverter {
    private final ModelMapper modelMapper;

    public DtoConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public FlightDto convertFlightToDto(Flight flight) {
        return modelMapper.map(flight, FlightDto.class);
    }

    public BookingDto convertBookingToDto(Booking booking) {
        return modelMapper.map(booking, BookingDto.class);
    }
}
