package com.utopia.bookingservice.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.utopia.bookingservice.entity.Payment;
import com.utopia.bookingservice.repository.PaymentRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    public void findByStripeId_ValidStripeId_PaymentFound() {
        String stripeId = "stripe_id";
        Optional<Payment> paymentOptional = Optional.of(new Payment());
        when(paymentRepository.findByStripeId(stripeId))
                .thenReturn(paymentOptional);

        Payment foundPayment = paymentService.findByStripeId(stripeId);
        assertThat(foundPayment, is(paymentOptional.get()));
    }

    @Test
    public void createPayment_ValidPayment_PaymentCreated() {
        Payment paymentToCreate = new Payment();
        when(paymentRepository.save(paymentToCreate))
                .thenReturn(paymentToCreate);

        Payment createdPayment = paymentService.createPayment(paymentToCreate);
        assertThat(createdPayment, is(paymentToCreate));
    }

    @Test
    public void deleteByStripeId_ValidStripeId_PaymentDeleted() {
        String stripeId = "stripe_id";
        when(paymentRepository.findByStripeId(stripeId))
                .thenReturn(Optional.of(new Payment()));

        paymentService.deleteByStripeId(stripeId);
        verify(paymentRepository, times(1)).deleteByStripeId(stripeId);
    }

    @Test
    public void createPaymentIntent_ValidPaymentInfo_PaymentIntentCreated()
            throws ResponseStatusException, StripeException {
        Map<String, Object> paymentInfo = new HashMap<String, Object>();
        try (MockedStatic<PaymentIntent> paymentIntentMock = mockStatic(
                PaymentIntent.class)) {
            PaymentIntent createdPaymentIntent = new PaymentIntent();
            paymentIntentMock.when(() -> PaymentIntent.create(paymentInfo))
                    .thenReturn(createdPaymentIntent);

            PaymentIntent returnedPaymentIntent = paymentService
                    .createPaymentIntent(paymentInfo);
            assertThat(returnedPaymentIntent, is(createdPaymentIntent));
        }
    }
}
