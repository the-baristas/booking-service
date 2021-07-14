package com.utopia.bookingservice.controller;

import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.stripe.model.PaymentIntent;
import com.utopia.bookingservice.dto.PaymentDto;
import com.utopia.bookingservice.dto.PaymentIntentInfoDto;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Payment;
import com.utopia.bookingservice.service.PaymentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PaymentControllerTest {

    private WebTestClient webTestClient;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Value("${jwt.secret-key")
    private String jwtSecretKey;

    @BeforeEach
    public void setUp() {
        webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build();
    }

    @Test
    @WithMockUser(authorities = { "ROLE_CUSTOMER" })
    public void testCreatePaymentIntent() throws Exception {
        PaymentIntentInfoDto paymentInfo = new PaymentIntentInfoDto(9001,
                "usd");
        HashMap<String, Object> paymentInfoMap = new HashMap<String, Object>();
        paymentInfoMap.put("amount", paymentInfo.getAmount());
        paymentInfoMap.put("currency", paymentInfo.getCurrency());
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setClientSecret("client_secret");
        when(paymentService.createPaymentIntent(paymentInfoMap))
                .thenReturn(paymentIntent);
        String jwtToken = JWT.create().withSubject("username")
                .withExpiresAt(new Date(System.currentTimeMillis() + 900_000))
                .sign(Algorithm.HMAC512(jwtSecretKey.getBytes()));

        webTestClient.post().uri("/payments/payment-intent")
                .headers((HttpHeaders headers) -> {
                    headers.add("Authorization", jwtToken);
                }).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentInfo).exchange().expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class)
                .isEqualTo("{\"clientSecret\":\"client_secret\"}");
    }

    @Test
    @WithMockUser(authorities = { "ROLE_CUSTOMER" })
    void testCreatePayment() {
        PaymentDto paymentDto = new PaymentDto(1L, "stripeid", false);
        Booking booking = new Booking();
        booking.setId(paymentDto.getBookingId());
        Payment payment = new Payment(booking, paymentDto.getStripeId(),
                paymentDto.isRefunded());

        when(paymentService.createPayment(payment)).thenReturn(payment);

        webTestClient.post().uri("/payments")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(paymentDto)
                .exchange().expectStatus().isOk().expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(PaymentDto.class).isEqualTo(paymentDto);
    }

    @Test
    void testFindByStripeId(){
        PaymentDto paymentDto = new PaymentDto(1L, "stripeid", false);
        Booking booking = new Booking();
        booking.setId(paymentDto.getBookingId());
        Payment payment = new Payment(booking, paymentDto.getStripeId(),
                paymentDto.isRefunded());

        when(paymentService.findByStripeId(payment.getStripeId())).thenReturn(payment);

        webTestClient.get().uri("/payments/" + payment.getStripeId())
                .exchange().expectStatus().isOk().expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(PaymentDto.class).isEqualTo(paymentDto);
    }

    @Test
    void testDeletePayment(){
        PaymentDto paymentDto = new PaymentDto(1L, "stripeid", false);
        Booking booking = new Booking();
        booking.setId(paymentDto.getBookingId());
        Payment payment = new Payment(booking, paymentDto.getStripeId(),
                paymentDto.isRefunded());

        webTestClient.delete().uri("/payments/" + payment.getStripeId())
                .exchange().expectStatus().isNoContent();
    }

}
