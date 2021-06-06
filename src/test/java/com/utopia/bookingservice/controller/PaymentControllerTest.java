package com.utopia.bookingservice.controller;

import com.stripe.exception.StripeException;

import com.utopia.bookingservice.dto.PaymentIntentInfoDto;
import com.utopia.bookingservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    private WebTestClient webTestClient;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    public void setUp() {
        webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build();
    }

    @Test
    public void testCreatePaymentIntent() throws StripeException {
        PaymentIntentInfoDto paymentInfo = new PaymentIntentInfoDto(9001, "usd", 1L);
        HashMap<String, Object> paymentInfoMap = new HashMap<String, Object>();
        paymentInfoMap.put("amount", paymentInfo.getAmount());
        paymentInfoMap.put("currency", paymentInfo.getCurrency());

        when(paymentService.createPaymentIntent(paymentInfoMap, paymentInfo.getBookingId())).thenReturn("clientSecretHushHush");

        webTestClient.post().uri("/payments/payment-intent")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(paymentInfo)
                .exchange().expectStatus().isOk().expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class).isEqualTo("{\"clientSecret\":\"clientSecretHushHush\"}");
    }

}
