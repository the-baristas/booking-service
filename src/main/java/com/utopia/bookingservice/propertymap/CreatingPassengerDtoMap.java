package com.utopia.bookingservice.propertymap;

import com.utopia.bookingservice.dto.PassengerCreationDto;
import com.utopia.bookingservice.entity.Passenger;

import org.modelmapper.PropertyMap;

public class CreatingPassengerDtoMap
        extends PropertyMap<PassengerCreationDto, Passenger> {
    @Override
    protected void configure() {
        map().getBooking()
                .setConfirmationCode(source.getBookingConfirmationCode());

        map().getFlight().getRoute().getOriginAirport()
                .setAirportCode(source.getOriginAirportCode());
        map().getFlight().getRoute().getDestinationAirport()
                .setAirportCode(source.getDestinationAirportCode());

        map().getFlight().getAirplane().setModel(source.getAirplaneModel());

        map().getFlight().setDepartureTime(source.getDepartureTime());
        map().getFlight().setArrivalTime(source.getArrivalTime());

        map().getDiscount().setDiscountType("none");
    }
}
