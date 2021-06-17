package com.utopia.bookingservice.repository;

import java.util.Optional;

import com.utopia.bookingservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByStripeId(String stripeId);

    void deleteByStripeId(String stripeId);
}
