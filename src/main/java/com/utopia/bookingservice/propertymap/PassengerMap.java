package com.utopia.bookingservice.propertymap;

import com.utopia.bookingservice.dto.PassengerResponseDto;
import com.utopia.bookingservice.entity.Passenger;

import org.modelmapper.PropertyMap;

public class PassengerMap extends PropertyMap<Passenger, PassengerResponseDto> {
    @Override
    protected void configure() {
        map().setBookingActive(source.getBooking().getActive());
        map().setBookingConfirmationCode(
                source.getBooking().getConfirmationCode());
        map().setLayoverCount(source.getBooking().getLayoverCount());
        map().setBookingTotalPrice(source.getBooking().getTotalPrice());

        map().setUsername(source.getBooking().getUser().getUsername());
        map().setEmail(source.getBooking().getUser().getEmail());
        map().setPhone(source.getBooking().getUser().getPhone());

        map().setFlightActive(source.getFlight().getActive());
        map().setDepartureTime(source.getFlight().getDepartureTime());
        map().setArrivalTime(source.getFlight().getArrivalTime());

        map().setRouteId(source.getFlight().getRoute().getId());
        map().setRouteActive(source.getFlight().getRoute().getActive());

        map().setOriginAirportCode(source.getFlight().getRoute()
                .getOriginAirport().getAirportCode());
        map().setOriginAirportActive(
                source.getFlight().getRoute().getOriginAirport().getActive());
        map().setOriginAirportCity(
                source.getFlight().getRoute().getOriginAirport().getCity());

        map().setDestinationAirportCode(source.getFlight().getRoute()
                .getDestinationAirport().getAirportCode());
        map().setDestinationAirportActive(source.getFlight().getRoute()
                .getDestinationAirport().getActive());
        map().setDestinationAirportCity(source.getFlight().getRoute()
                .getDestinationAirport().getCity());

        map().setDiscountType(source.getDiscount().getDiscountType());
        map().setDiscountRate(source.getDiscount().getDiscountRate());
    }
}
