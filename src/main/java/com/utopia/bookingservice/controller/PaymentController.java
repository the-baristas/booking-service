package com.utopia.bookingservice.controller;

import java.text.ParseException;
import java.util.HashMap;

import com.stripe.exception.StripeException;
import com.utopia.bookingservice.dto.PaymentDto;
import com.utopia.bookingservice.dto.PaymentIntentInfoDto;
import com.utopia.bookingservice.entity.Payment;
import com.utopia.bookingservice.service.PaymentService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/payments")
@CrossOrigin
public class PaymentController {

    private ModelMapper modelMapper;

    @Autowired
    private PaymentService paymentService;

    public PaymentController(PaymentService paymentService,
            ModelMapper modelMapper) {
        this.paymentService = paymentService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/health")
    public String health() {
        return "ye";
    }

    @GetMapping("{stripe_id}")
    public ResponseEntity<PaymentDto> findByStripeId(
            @PathVariable("stripe_id") String stripeId) {
        Payment payment = paymentService.findByStripeId(stripeId);
        PaymentDto paymentDto = modelMapper.map(payment, PaymentDto.class);
        return ResponseEntity.ok(paymentDto);
    }

    @PostMapping("/payment-intent")
    public HashMap<String, String> createPaymentIntent(
            @RequestBody PaymentIntentInfoDto paymentInfo)
            throws StripeException {

        // Stripe API requires a Map<String, Object>
        HashMap<String, Object> paymentInfoMap = new HashMap<String, Object>();
        paymentInfoMap.put("amount", paymentInfo.getAmount());
        paymentInfoMap.put("currency", paymentInfo.getCurrency());

        // return the clientSecret which was just created
        HashMap<String, String> toReturn = new HashMap<String, String>();
        toReturn.put("clientSecret",
                paymentService.createPaymentIntent(paymentInfoMap));
        return toReturn;
    }

    @PostMapping("")
    public PaymentDto createPayment(@RequestBody PaymentDto paymentDto)
            throws ParseException {
        Payment payment = convertToEntity(paymentDto);
        payment.setStripeId(paymentDto.getStripeId());
        return convertToDto(paymentService.createPayment(payment));
    }

    @DeleteMapping("{stripe_id}")
    public ResponseEntity<Void> deletePayment(
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
