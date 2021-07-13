package com.utopia.bookingservice.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

@Data
public class DiscountDto {

    @NotBlank
    private String discountType;

    @PositiveOrZero
    private Double discountRate;

}
