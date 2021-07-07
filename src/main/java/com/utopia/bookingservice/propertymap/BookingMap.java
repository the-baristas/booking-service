package com.utopia.bookingservice.propertymap;

import com.utopia.bookingservice.dto.BookingResponseDto;
import com.utopia.bookingservice.entity.Booking;

import org.modelmapper.PropertyMap;

public class BookingMap extends PropertyMap<Booking, BookingResponseDto> {
    @Override
    protected void configure() {
        map().setUsername(source.getUser().getUsername());
        map().setEmail(source.getUser().getEmail());
        map().setPhone(source.getUser().getPhone());
    }
}
