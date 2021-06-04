package com.utopia.bookingservice.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreationDto {
    @NotBlank
    private String confirmationCode;

    @PositiveOrZero
    private Integer layoverCount;

    @NotBlank
    private String username;

    // @NotBlank
    // private String originAirportCode;

    // @NotBlank
    // private String destinationAirportCode;

    // @NotNull
    // private String airplaneModel;

    // @NotNull
    // private LocalDateTime departureTime;

    // @NotNull
    // private LocalDateTime arrivalTime;

    // @NotBlank
    // private String givenName;

    // @NotBlank
    // private String familyName;

    // @NotNull
    // private LocalDate dateOfBirth;

    // @NotBlank
    // private String gender;

    // @NotBlank
    // private String address;

    // @NotBlank
    // private String seatClass;

    // @Positive
    // private Integer seatNumber;

    // @Positive
    // private Integer checkInGroup;
}
