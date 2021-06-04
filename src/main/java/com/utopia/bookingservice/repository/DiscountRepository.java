package com.utopia.bookingservice.repository;

import java.util.Optional;

import com.utopia.bookingservice.entity.Discount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    public Optional<Discount> findByDiscountType(String discountType);
}
