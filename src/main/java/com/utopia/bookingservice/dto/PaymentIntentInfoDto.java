package com.utopia.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentInfoDto {

    @NotNull
    @Positive
    Integer amount;

    @NotNull
    String currency;

    @NotNull
    @PositiveOrZero
    Long bookingId;
}
