package com.utopia.bookingservice.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class RefundRequestDto {
    @NotNull
    @Positive
    Integer refundAmount;

    @NotNull
    Long bookingId;
}
