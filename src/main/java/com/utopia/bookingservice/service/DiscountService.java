package com.utopia.bookingservice.service;

import com.utopia.bookingservice.entity.Discount;
import com.utopia.bookingservice.repository.DiscountRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscountService {
    private final DiscountRepository discountRepository;

    public Discount findByDiscountType(String discountType) {
        return discountRepository.findByDiscountType(discountType).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find discount with discount type: "
                                + discountType));
    }
}
