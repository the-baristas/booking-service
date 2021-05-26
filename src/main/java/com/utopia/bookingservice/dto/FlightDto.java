package com.utopia.bookingservice.dto;

import java.time.ZonedDateTime;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightDto {
    @Positive
    private Long id;

    @NotNull
    private Boolean active;

    @NotNull
    private ZonedDateTime departureTime;

    @NotNull
    private ZonedDateTime arrivalTime;

    @Positive
    private Long routeId;

    @NotNull
    private Boolean routeActive;

    @NotBlank
    private String originAirportCode;

    @NotBlank
    private String originAirportCity;

    @NotNull
    private Boolean originAirportActive;

    @NotBlank
    private String destinationAirportCode;

    @NotBlank
    private String destinationAirportCity;

    @NotNull
    private Boolean destinationAirportActive;

    private List<PassengerDto> passengers;

    private List<BookingDto> bookings;
}
