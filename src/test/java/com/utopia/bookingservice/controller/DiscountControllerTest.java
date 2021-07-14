package com.utopia.bookingservice.controller;

import com.utopia.bookingservice.dto.DiscountDto;
import com.utopia.bookingservice.dto.PassengerResponseDto;
import com.utopia.bookingservice.entity.Discount;
import com.utopia.bookingservice.service.DiscountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiscountController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(authorities = { "ROLE_ADMIN" })
public class DiscountControllerTest {

    private WebTestClient webTestClient;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiscountService discountService;

    @BeforeEach
    public void setUp() {
        webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build();
    }

    @Test
    void getAllDiscountsTest() throws Exception {
        Discount d1 = makeDiscount();
        Discount d2 = makeDiscount();
        d2.setDiscountType("type2");
        List<Discount> discounts = new ArrayList<Discount>();
        discounts.add(d1); discounts.add(d2);

        Page<Discount> discountPage = new PageImpl<Discount>(discounts);

        when(discountService.findAll(0,2)).thenReturn(discountPage);

        mockMvc.perform(get("/discounts?index=0&size=2").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void updateDiscountTest() throws Exception {
        Discount discount = makeDiscount();
        DiscountDto dto = makeDiscountDto();

        when(discountService.update(discount)).thenReturn(discount);

        webTestClient.put().uri("/discounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DiscountDto.class)
                .isEqualTo(dto);
    }

    private Discount makeDiscount(){
        Discount discount = new Discount();
        discount.setDiscountRate(0.5);
        discount.setDiscountType("type");
        return discount;
    }

    private DiscountDto makeDiscountDto(){
        DiscountDto discount = new DiscountDto();
        discount.setDiscountRate(0.5);
        discount.setDiscountType("type");
        return discount;
    }
}
