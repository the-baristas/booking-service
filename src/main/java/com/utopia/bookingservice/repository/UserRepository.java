package com.utopia.bookingservice.repository;

import java.util.List;
import java.util.Optional;

import com.utopia.bookingservice.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findByUsername(String username);

    @Query("SELECT u.email FROM User u "
            + "JOIN u.bookings b "
            + "JOIN b.passengers p "
            + "JOIN p.flight f "
            + "WHERE f.id = :flightId")
    public List<String> findEmailsByFlightId(Long flightId);

    @Query("SELECT u.email FROM User u "
            + "JOIN Booking b ON u.id = b.user.id "
            + "JOIN Passenger p ON b.id = p.booking.id "
            + "JOIN Flight f ON p.flight.id = f.id "
            + "WHERE f.id = :flightId")
    public List<String> findEmailsByFlightId2(Long flightId);

    @Query("SELECT u.email FROM User u "
            + "JOIN u.bookings b "
            + "WHERE b.id = :bookingId")
    public List<String> findEmailsByBookingId(Long bookingId);
}
