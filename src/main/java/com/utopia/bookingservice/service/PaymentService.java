package com.utopia.bookingservice.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.utopia.bookingservice.entity.ChargeRequest;
import com.utopia.bookingservice.entity.Payment;
import com.utopia.bookingservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.xml.ws.Response;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${stripe.secretTestKey}")
    private String secretKey;

    @PostConstruct
    public void init(){
        Stripe.apiKey = secretKey;
    }

    public String createPaymentIntent(Map<String, Object> paymentInfo, Long bookingId)
            throws StripeException, ResponseStatusException {
        PaymentIntent intent = PaymentIntent.create(paymentInfo);

        Payment payment = new Payment(bookingId, intent.getClientSecret(), false);
        paymentRepository.save(payment);

        return intent.getClientSecret();
    }

    public String getSecretKey(){
        return secretKey;
    }
}
