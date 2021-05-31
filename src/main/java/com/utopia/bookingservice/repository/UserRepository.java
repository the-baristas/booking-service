package com.utopia.bookingservice.repository;

import java.util.Optional;

import com.utopia.bookingservice.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findByUsername(String username);
}
