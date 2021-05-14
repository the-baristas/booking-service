package com.utopia.bookingservice.dto;

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
public class BookingDto {
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
}
