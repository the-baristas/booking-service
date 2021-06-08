package com.utopia.bookingservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    @PositiveOrZero
    private Long bookingId;

    @NotBlank
    private String stripeId;

    @NotNull
    private boolean refunded;
}
