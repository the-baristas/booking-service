package com.utopia.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingPurchaseDto {
    @PositiveOrZero
    private Integer layoverCount;

    @NotBlank
    private String username;

    @NotBlank
    private String stripeId;

    @PositiveOrZero
    private Long totalPrice;

    @NotEmpty
    private List<PassengerPurchaseDto> passengers;

}
