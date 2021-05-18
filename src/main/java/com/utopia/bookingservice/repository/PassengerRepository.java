package com.utopia.bookingservice.repository;

import com.utopia.bookingservice.entity.Passenger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    @Query("SELECT p FROM Passenger p WHERE p.booking.confirmationCode LIKE %:searchTerm% OR p.booking.user.username LIKE %:searchTerm%")
    Page<Passenger> findByConfirmationCodeOrUsernameContaining(
            @Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Passenger p WHERE p.booking.confirmationCode LIKE %:searchTerm% OR p.booking.user.username LIKE %:searchTerm%")
    Page<Passenger> findDistinctByConfirmationCodeOrUsernameContaining(
            @Param("searchTerm") String searchTerm, Pageable pageable);
}
