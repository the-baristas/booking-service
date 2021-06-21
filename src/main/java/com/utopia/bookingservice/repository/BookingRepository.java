package com.utopia.bookingservice.repository;

import java.util.List;
import java.util.Optional;

import com.utopia.bookingservice.entity.Booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByConfirmationCode(String confirmationCode);

    List<Booking> findByConfirmationCodeContaining(String confirmationCode);

    @Query("Select b FROM Booking b WHERE b.user.username = :username ORDER BY b.id DESC")
    Page<Booking> findByUsername(String username, Pageable pageable);
}
