package com.utopia.bookingservice.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingUpdateDto {
    @NotBlank
    private String confirmationCode;

    @PositiveOrZero
    private Integer layoverCount;

    @PositiveOrZero
    private Double totalPrice;

    @NotBlank
    private String username;
}
