package com.utopia.bookingservice.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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

    @NotNull
    private Boolean active;

    @PositiveOrZero
    private Integer layoverCount;

    @PositiveOrZero
    private Double totalPrice;
}
