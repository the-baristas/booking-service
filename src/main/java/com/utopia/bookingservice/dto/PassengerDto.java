package com.utopia.bookingservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDto {
    private Long id;

    @Positive
    private Long bookingId;

    @NotNull
    private Boolean bookingActive;

    @NotBlank
    private String bookingConfirmationCode;

    @PositiveOrZero
    private Integer layoverCount;

    @PositiveOrZero
    private Double bookingTotalPrice;

    @Positive
    private Long flightId;

    @NotNull
    private Boolean flightActive;

    @NotNull
    private LocalDateTime departureTime;

    @NotNull
    private LocalDateTime arrivalTime;

    @Positive
    private Long routeId;

    @NotNull
    private Boolean routeActive;

    @NotBlank
    private String originAirportCode;

    @NotNull
    private Boolean originAirportActive;

    @NotBlank
    private String originAirportCity;

    @NotBlank
    private String destinationAirportCode;

    @NotNull
    private Boolean destinationAirportActive;

    @NotBlank
    private String destinationAirportCity;

    @NotBlank
    private String discountType;

    @PositiveOrZero
    private Double discountRate;

    @NotBlank
    private String givenName;

    @NotBlank
    private String familyName;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    private String gender;

    @NotBlank
    private String address;

    @NotBlank
    private String seatClass;

    @Positive
    private Integer seatNumber;

    @Positive
    private Integer checkInGroup;

    @NotBlank
    private String username;

    @Email
    private String email;

    @NotBlank
    private String phone;
}
