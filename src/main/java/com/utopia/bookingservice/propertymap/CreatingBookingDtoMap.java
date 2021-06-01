package com.utopia.bookingservice.propertymap;

import com.utopia.bookingservice.dto.CreatingBookingDto;
import com.utopia.bookingservice.entity.Booking;

import org.modelmapper.PropertyMap;

public class CreatingBookingDtoMap
        extends PropertyMap<CreatingBookingDto, Booking> {
    @Override
    protected void configure() {
        map().getUser().setUsername(source.getUsername());
    }
}
