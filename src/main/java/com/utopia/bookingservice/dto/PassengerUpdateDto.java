package com.utopia.bookingservice.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerUpdateDto {
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
}
