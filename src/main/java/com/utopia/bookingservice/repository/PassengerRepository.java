package com.utopia.bookingservice.repository;

import com.utopia.bookingservice.entity.Passenger;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
}
