package com.utopia.bookingservice.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentInfoDto {

    @NotNull
    @Positive
    Integer amount;

    @NotNull
    String currency;
}
