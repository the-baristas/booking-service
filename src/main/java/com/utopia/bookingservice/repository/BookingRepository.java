package com.utopia.bookingservice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.utopia.bookingservice.entity.Booking;

import com.utopia.bookingservice.entity.Passenger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByConfirmationCode(String confirmationCode);

    Page<Booking> findByConfirmationCodeContaining(String confirmationCode,
            Pageable pageable);

    @Query("Select b FROM Booking b WHERE b.user.username = :username ORDER BY b.id DESC")
    Page<Booking> findByUsername(String username, Pageable pageable);

    @Query("Select b FROM Booking b WHERE b.user.username = :username ORDER BY b.id DESC")
    List<Booking> findAllByUsername(String username);
}
