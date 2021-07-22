package com.utopia.bookingservice.repository;

import java.time.LocalDate;

import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.entity.User;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserRepositoryTest {
    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    private UserRepository userRepository;

    @Disabled
    @Test
    public void findEmails() {
        Passenger passenger = new Passenger();
        String givenName = "first_name";
        String familyName = "family_name";
        LocalDate dateOfBirth = LocalDate.of(2000, 1, 1);
        String gender = "nonbinary";
        String address = "1 Main Street Test City, FL 12345";
        String seatClass = "first";
        Integer seatNumber = 1;
        Integer checkInGroup = 1;
        passenger.setId(1L);
        passenger.setGivenName(givenName);
        passenger.setFamilyName(familyName);
        passenger.setDateOfBirth(dateOfBirth);
        passenger.setGender(gender);
        passenger.setAddress(address);
        passenger.setSeatClass(seatClass);
        passenger.setSeatNumber(seatNumber);
        passenger.setCheckInGroup(checkInGroup);
        passenger.setBooking(new Booking());
        passenger.getBooking().setId(1L);
        testEntityManager.merge(passenger);
        testEntityManager.flush();

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setActive(Boolean.TRUE);
        booking.setConfirmationCode("A1");
        booking.setLayoverCount(0);
        booking.setTotalPrice(1.01);

        User user = new User();
        user.setId(1L);
        booking.setUser(user);
        testEntityManager.merge(user);
        testEntityManager.flush();
    }
}
