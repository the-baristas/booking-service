package com.utopia.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerPurchaseDto {

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

    @Positive
    private Long flightId;

}
