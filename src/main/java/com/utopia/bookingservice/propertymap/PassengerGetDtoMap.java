package com.utopia.bookingservice.propertymap;

import com.utopia.bookingservice.dto.PassengerResponseDto;
import com.utopia.bookingservice.entity.Passenger;

import org.modelmapper.PropertyMap;

public class PassengerGetDtoMap extends PropertyMap<PassengerResponseDto, Passenger> {
    @Override
    protected void configure() {
        map().getBooking().setId(source.getBookingId());
        map().getBooking().setActive(source.getBookingActive());
        map().getBooking()
                .setConfirmationCode(source.getBookingConfirmationCode());
        map().getBooking().setLayoverCount(source.getLayoverCount());
        map().getBooking().setTotalPrice(source.getBookingTotalPrice());

        map().getBooking().getUser().setUsername(source.getUsername());

        map().getFlight().setId(source.getFlightId());
        map().getFlight().setActive(source.getFlightActive());
        map().getFlight().setDepartureTime(source.getDepartureTime());
        map().getFlight().setArrivalTime(source.getArrivalTime());

        map().getFlight().getRoute().setId(source.getRouteId());
        map().getFlight().getRoute().setActive(source.getRouteActive());

        map().getFlight().getRoute().getOriginAirport()
                .setAirportCode(source.getOriginAirportCode());
        map().getFlight().getRoute().getOriginAirport()
                .setActive(source.getOriginAirportActive());
        map().getFlight().getRoute().getOriginAirport()
                .setCity(source.getOriginAirportCity());

        map().getFlight().getRoute().getDestinationAirport()
                .setAirportCode(source.getDestinationAirportCode());
        map().getFlight().getRoute().getDestinationAirport()
                .setActive(source.getDestinationAirportActive());
        map().getFlight().getRoute().getDestinationAirport()
                .setCity(source.getDestinationAirportCity());

        map().getDiscount().setDiscountType(source.getDiscountType());
        map().getDiscount().setDiscountRate(source.getDiscountRate());
    }
}
