package com.utopia.bookingservice.dto;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class BookingResponseDto {
    @Positive
    private Long id;

    @NotNull
    private Boolean active;

    @NotBlank
    private String confirmationCode;

    @PositiveOrZero
    private Integer layoverCount;

    @PositiveOrZero
    private Double totalPrice;

    @NotBlank
    private String username;

    @Email
    private String email;

    @NotBlank
    private String phone;

    @NotNull
    private List<FlightDto> flights;

    @NotNull
    private List<PassengerResponseDto> passengers;

    @NotBlank
    private String stripeId;

    @NotNull
    private Boolean refunded;
}
