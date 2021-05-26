package com.utopia.bookingservice.repository;

import java.util.List;

import com.utopia.bookingservice.entity.Flight;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    // @Query(value = "SELECT flight.id, flight.is_active FROM booking_flight
    // JOIN booking on booking_id = booking.id JOIN flight ON flight_id =
    // flight.id where booking.confirmation_code = 'C1';",
    // nativeQuery = true)
    @Query("SELECT f FROM Booking b JOIN b.flights f WHERE b.confirmationCode = :confirmationCode")
    List<Flight> getBookingFlights(
            @Param("confirmationCode") String confirmationCode);
}
