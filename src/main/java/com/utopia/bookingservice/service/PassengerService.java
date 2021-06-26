package com.utopia.bookingservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Discount;
import com.utopia.bookingservice.entity.Flight;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.repository.BookingRepository;
import com.utopia.bookingservice.repository.DiscountRepository;
import com.utopia.bookingservice.repository.FlightRepository;
import com.utopia.bookingservice.repository.PassengerRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PassengerService {
    private static final Double LAYOVER_DISCOUNT_RATE = 0.1;

    private final PassengerRepository passengerRepository;
    private final FlightRepository flightRepository;
    private final DiscountRepository discountRepository;
    private final BookingRepository bookingRepository;

    public Page<Passenger> findAll(Integer pageIndex, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return passengerRepository.findAll(pageable);
    }

    public Passenger findById(Long id) {
        return passengerRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find passenger with id=" + id));
    }

    public Passenger create(Passenger passengerToCreate,
            String originAirportCode, String destinationAirportCode,
            String airplaneModel, LocalDateTime departureTime,
            LocalDateTime arrivalTime, String seatClass,
            LocalDate dateOfBirth) {
        Flight flight = flightRepository
                .identifyFlight(originAirportCode, destinationAirportCode,
                        airplaneModel, departureTime, arrivalTime)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Could not identify flight using " + originAirportCode
                                + ", " + destinationAirportCode + ", "
                                + airplaneModel + ", " + departureTime + ", "
                                + arrivalTime));
        passengerToCreate.setFlight(flight);

        Double basePrice = 0d;
        switch (seatClass) {
            case "first":
                basePrice = flight.getFirstClassPrice();
                break;
            case "business":
                basePrice = flight.getBusinessClassPrice();
                break;
            case "economy":
                basePrice = flight.getEconomyClassPrice();
                break;
            default:
                break;
        }
        Integer age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        String discountType;
        if (age <= 2) {
            discountType = "child";
        } else if (age >= 65) {
            discountType = "elderly";
        } else {
            discountType = "none";
        }
        Discount discount = discountRepository.findByDiscountType(discountType)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Could not find discount with discount type: "
                                        + discountType));
        passengerToCreate.setDiscount(discount);
        Double discountRate = discount.getDiscountRate();
        String confirmationCode = passengerToCreate.getBooking()
                .getConfirmationCode();
        Booking booking = bookingRepository
                .findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Could not find booking with confirmation code: "
                                + confirmationCode));
        Integer layoverCount = booking.getLayoverCount();
        Double totalPrice = booking.getTotalPrice()
                + +calculateTotalPrice(basePrice, discountRate, layoverCount);
        booking.setTotalPrice(totalPrice);
        passengerToCreate.setBooking(booking);

        try {
            return passengerRepository.save(passengerToCreate);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not create passenger with id="
                            + passengerToCreate.getId(),
                    e);
        }
    }

    private Double calculateTotalPrice(Double basePrice, Double discountRate,
            Integer layoverCount) {
        if (layoverCount > 0) {
            discountRate += LAYOVER_DISCOUNT_RATE;
        }
        return basePrice * discountRate;
    }

    public Passenger update(Passenger passenger) {
        passengerRepository.findById(passenger.getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find passenger with id="
                                + passenger.getId()));
        return passengerRepository.save(passenger);
    }

    public void deleteById(Long id) {
        passengerRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find passenger with id=" + id));
        passengerRepository.deleteById(id);
    }

    public Page<Passenger> findByConfirmationCodeOrUsernameContaining(
            String searchTerm, Integer pageIndex, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return passengerRepository.findByConfirmationCodeOrUsernameContaining(
                searchTerm, pageable);
    }

    public Page<Passenger> findDistinctByConfirmationCodeOrUsernameContaining(
            String searchTerm, Integer pageIndex, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return passengerRepository
                .findDistinctByConfirmationCodeOrUsernameContaining(searchTerm,
                        pageable);
    }
}
