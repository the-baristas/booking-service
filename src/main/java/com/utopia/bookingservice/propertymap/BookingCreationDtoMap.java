package com.utopia.bookingservice.propertymap;

import com.utopia.bookingservice.dto.BookingCreationDto;
import com.utopia.bookingservice.entity.Booking;

import org.modelmapper.PropertyMap;

public class BookingCreationDtoMap
        extends PropertyMap<BookingCreationDto, Booking> {
    @Override
    protected void configure() {
        map().getUser().setUsername(source.getUsername());
    }
}
