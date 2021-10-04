package com.utopia.bookingservice.controller;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.utopia.bookingservice.dto.PaymentDto;
import com.utopia.bookingservice.dto.PaymentIntentInfoDto;
import com.utopia.bookingservice.entity.Payment;
import com.utopia.bookingservice.service.PaymentService;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping(path = "/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final ModelMapper modelMapper;

    public PaymentController(PaymentService paymentService,
            ModelMapper modelMapper) {
        this.paymentService = paymentService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("{stripe_id}")
    public ResponseEntity<PaymentDto> findByStripeId(
            @PathVariable("stripe_id") String stripeId) {
        Payment payment = paymentService.findByStripeId(stripeId);
        PaymentDto paymentDto = convertToDto(payment);
        return ResponseEntity.ok(paymentDto);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_AGENT')")
    @PostMapping("/payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @Valid @RequestBody PaymentIntentInfoDto paymentInfo,
            UriComponentsBuilder builder) throws StripeException {

        // Stripe API requires a Map<String, Object>
        Map<String, Object> paymentInfoMap = new HashMap<>();
        paymentInfoMap.put("amount", paymentInfo.getAmount());
        paymentInfoMap.put("currency", paymentInfo.getCurrency());

        PaymentIntent paymentIntent = paymentService
                .createPaymentIntent(paymentInfoMap);
        String id = paymentIntent.getId();
        // return the clientSecret which was just created
        Map<String, String> clientSecretMap = new HashMap<>();
        clientSecretMap.put("clientSecret", paymentIntent.getClientSecret());
        return ResponseEntity
                .created(builder.path("/payment-intent/{id}").build(id))
                .body(clientSecretMap);
    }

    @PostMapping
    public PaymentDto createPayment(@RequestBody PaymentDto paymentDto)
            throws ParseException {
        Payment payment = convertToEntity(paymentDto);
        payment.setStripeId(paymentDto.getStripeId());
        return convertToDto(paymentService.createPayment(payment));
    }

    @DeleteMapping("{stripe_id}")
    public ResponseEntity<Void> deletePaymentByStripeId(
            @PathVariable("stripe_id") String stripeId) {
        paymentService.deleteByStripeId(stripeId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(StripeException.class)
    public String handleError(StripeException e) {
        return e.getMessage();
    }

    private PaymentDto convertToDto(Payment payment) {
        return modelMapper.map(payment, PaymentDto.class);
    }

    private Payment convertToEntity(PaymentDto paymentDto)
            throws ParseException {
        return modelMapper.map(paymentDto, Payment.class);
    }
}
