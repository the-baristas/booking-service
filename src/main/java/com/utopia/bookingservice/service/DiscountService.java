package com.utopia.bookingservice.service;

import com.utopia.bookingservice.entity.Discount;
import com.utopia.bookingservice.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class DiscountService {

    @Autowired
    DiscountRepository discountRepository;

    public Page<Discount> findAll(Integer pageIndex, Integer pageSize){
        return discountRepository.findAll(PageRequest.of(pageIndex, pageSize));
    }

    public Discount update(Discount discount){
        return discountRepository.save(discount);
    }

}
