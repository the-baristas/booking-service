package com.utopia.bookingservice.service;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.utopia.bookingservice.entity.Payment;
import com.utopia.bookingservice.repository.PaymentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public Payment findByStripeId(String stripeId) {
        return paymentRepository.findByStripeId(stripeId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Payment createPayment(Payment payment) {
        try {
            return paymentRepository.save(payment);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not create payment for booking with id: "
                            + payment.getBooking().getId(),
                    e);
        }
    }

    @Transactional
    public void deleteByStripeId(String stripeId) {
        paymentRepository.findByStripeId(stripeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Could not find payment with stripd ID: " + stripeId));
        try {
            paymentRepository.deleteByStripeId(stripeId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not delete payment with stripe ID: " + stripeId, e);
        }
    }

    public PaymentIntent createPaymentIntent(Map<String, Object> paymentInfo)
            throws StripeException, ResponseStatusException {
        return PaymentIntent.create(paymentInfo);
    }
}
