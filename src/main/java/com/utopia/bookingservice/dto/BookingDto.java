package com.utopia.bookingservice.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import com.utopia.bookingservice.entity.Flight;

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

    @NotNull
    private List<Flight> flights;
}
