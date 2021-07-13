package com.utopia.bookingservice.controller;

import com.utopia.bookingservice.dto.DiscountDto;
import com.utopia.bookingservice.dto.PaymentDto;
import com.utopia.bookingservice.entity.Discount;
import com.utopia.bookingservice.entity.Payment;
import com.utopia.bookingservice.service.DiscountService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;

@RestController
@RequestMapping(path = "/discounts")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @Autowired
    private ModelMapper modelMapper;


    @GetMapping
    public Page<DiscountDto> findAll(@RequestParam("index") Integer pageIndex,
                                     @RequestParam("size") Integer pageSize){
        Page<Discount> discounts = discountService.findAll(pageIndex, pageSize);
        return discounts.map(this::convertToDto);
    }

    @PutMapping
    public DiscountDto update(@Valid @RequestBody DiscountDto discountDto){
        Discount discount = convertToEntity((discountDto));
        return convertToDto(discountService.update(discount));
    }

    private DiscountDto convertToDto(Discount discount) {
        return modelMapper.map(discount, DiscountDto.class);
    }

    private Discount convertToEntity(DiscountDto discountDto) {
        return modelMapper.map(discountDto, Discount.class);
    }
}
