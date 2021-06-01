package com.utopia.bookingservice.repository;

import java.util.List;
import java.util.Optional;

import com.utopia.bookingservice.entity.Booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByConfirmationCode(String confirmationCode);

    List<Booking> findByConfirmationCodeContaining(String confirmationCode);
}
