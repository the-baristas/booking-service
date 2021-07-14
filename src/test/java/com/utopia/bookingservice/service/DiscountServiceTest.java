package com.utopia.bookingservice.service;

import com.utopia.bookingservice.entity.Discount;
import com.utopia.bookingservice.repository.DiscountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
public class DiscountServiceTest {

    @Mock
    private DiscountRepository repository;

    @InjectMocks
    private DiscountService service;

    @Test
    void testUpdateDiscount(){
        Discount discount = makeDiscount();

        when(repository.save(discount)).thenReturn(discount);

        assertThat(service.update(discount), is(discount));
    }

    @Test
    void testGetAllDiscounts(){
        Discount discount = makeDiscount();
        List<Discount> discounts = new ArrayList<Discount>();
        discounts.add(discount);
        Page<Discount> discountPage = new PageImpl<Discount>(discounts);

        PageRequest pageRequest = PageRequest.of(0, 1);

        when(repository.findAll(pageRequest)).thenReturn(discountPage);

        assertThat(service.findAll(0,1), is(discountPage));
    }

    private Discount makeDiscount(){
        Discount discount = new Discount();
        discount.setDiscountRate(0.5);
        discount.setDiscountType("type");
        return discount;
    }

}
