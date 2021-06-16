package com.utopia.bookingservice.service;

import java.util.Map;

import javax.annotation.PostConstruct;

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

    @Value("${STRIPE_SECRET_KEY}")
    private String secretKey;

    @PostConstruct
    public void init(){
        Stripe.apiKey = secretKey;
    }

    public Payment createPayment(Payment payment){
        try {
            return paymentRepository.save(payment);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not create payment for booking with id: " + payment.getBooking().getId(), e);
        }
    }

    public String createPaymentIntent(Map<String, Object> paymentInfo)
            throws StripeException, ResponseStatusException {
        PaymentIntent intent = PaymentIntent.create(paymentInfo);

        return intent.getClientSecret();
    }
}
