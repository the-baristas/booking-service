package com.utopia.bookingservice.repository;

import com.utopia.bookingservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
