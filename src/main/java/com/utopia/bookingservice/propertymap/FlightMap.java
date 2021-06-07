package com.utopia.bookingservice.propertymap;

import com.utopia.bookingservice.dto.FlightDto;
import com.utopia.bookingservice.entity.Flight;

import org.modelmapper.PropertyMap;

public class FlightMap extends PropertyMap<Flight, FlightDto> {
    @Override
    protected void configure() {
        map().setRouteId(source.getRoute().getId());
        map().setRouteActive(source.getRoute().getActive());

        map().setOriginAirportCode(
                source.getRoute().getOriginAirport().getAirportCode());
        map().setOriginAirportCity(
                source.getRoute().getOriginAirport().getCity());
        map().setOriginAirportActive(
                source.getRoute().getOriginAirport().getActive());
        map().setDestinationAirportCode(
                source.getRoute().getDestinationAirport().getAirportCode());
        map().setDestinationAirportCity(
                source.getRoute().getDestinationAirport().getCity());
        map().setDestinationAirportActive(
                source.getRoute().getDestinationAirport().getActive());

        map().setAirplaneModel(source.getAirplane().getModel());
    }
}
