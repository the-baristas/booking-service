package com.utopia.bookingservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

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
    private static final Integer CHILD_DISCOUNT_AGE = 12;
    private static final Integer ELDERLY_DISCOUNT_AGE = 65;
    private static final String CHILD_DISCOUNT_TYPE = "child";
    private static final String ELDERLY_DISCOUNT_TYPE = "elderly";
    private static final String NONE_DISCOUNT_TYPE = "none";
    private static final String FIRST_CLASS = "first";
    private static final String BUSINESS_CLASS = "business";
    private static final String ECONOMY_CLASS = "economy";

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
        Flight flight = getFlight(originAirportCode, destinationAirportCode,
                airplaneModel, departureTime, arrivalTime);
        incrementReservedSeatsCount(seatClass, flight);
        passengerToCreate.setFlight(flight);

        Discount discount = getDiscount(dateOfBirth);
        passengerToCreate.setDiscount(discount);

        Double discountRate = discount.getDiscountRate();
        String confirmationCode = passengerToCreate.getBooking()
                .getConfirmationCode();
        Booking booking = getBooking(confirmationCode);
        Integer layoverCount = booking.getLayoverCount();
        Double currentBookingTotalPrice = booking.getTotalPrice();
        Double passengerTotalPrice = calculatePassengerTotalPrice(seatClass,
                flight, discountRate, layoverCount);
        Double totalPrice = currentBookingTotalPrice + passengerTotalPrice;
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

    private Flight getFlight(String originAirportCode,
            String destinationAirportCode, String airplaneModel,
            LocalDateTime departureTime, LocalDateTime arrivalTime) {
        return flightRepository
                .identifyFlight(originAirportCode, destinationAirportCode,
                        airplaneModel, departureTime, arrivalTime)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Could not identify flight using " + originAirportCode
                                + ", " + destinationAirportCode + ", "
                                + airplaneModel + ", " + departureTime + ", "
                                + arrivalTime));
    }

    private Booking getBooking(String confirmationCode) {
        return bookingRepository.findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Could not find booking with confirmation code: "
                                + confirmationCode));
    }

    private Double calculatePassengerTotalPrice(String seatClass, Flight flight,
            Double discountRate, Integer layoverCount) {
        if (layoverCount > 0) {
            discountRate += LAYOVER_DISCOUNT_RATE;
        }
        Double basePrice = getBasePrice(seatClass, flight);
        return basePrice * discountRate;
    }

    private Discount getDiscount(LocalDate dateOfBirth) {
        Integer age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        String discountType;
        if (age <= CHILD_DISCOUNT_AGE) {
            discountType = CHILD_DISCOUNT_TYPE;
        } else if (age >= ELDERLY_DISCOUNT_AGE) {
            discountType = ELDERLY_DISCOUNT_TYPE;
        } else {
            discountType = NONE_DISCOUNT_TYPE;
        }
        return discountRepository.findByDiscountType(discountType).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find discount with discount type: "
                                + discountType));
    }

    private Double getBasePrice(String seatClass, Flight flight) {
        Double basePrice;
        switch (seatClass) {
            case FIRST_CLASS:
                basePrice = flight.getFirstClassPrice();
                break;
            case BUSINESS_CLASS:
                basePrice = flight.getBusinessClassPrice();
                break;
            case ECONOMY_CLASS:
                basePrice = flight.getEconomyClassPrice();
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid flight seat class: " + seatClass);
        }
        return basePrice;
    }

    public Passenger update(Long id, String givenName, String familyName,
            LocalDate dateOfBirth, String gender, String address,
            String newSeatClass, Integer seatNumber, Integer checkInGroup) {
        Passenger passenger = passengerRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find passenger with id=" + id));
        String currentSeatClass = passenger.getSeatClass();
        if (newSeatClass != currentSeatClass) {
            incrementReservedSeatsCount(newSeatClass, passenger.getFlight());
            decrementReservedSeatsCount(currentSeatClass,
                    passenger.getFlight());
        }
        passenger.setGivenName(givenName);
        passenger.setFamilyName(familyName);
        passenger.setDateOfBirth(dateOfBirth);
        passenger.setGender(gender);
        passenger.setAddress(address);
        passenger.setSeatClass(newSeatClass);
        passenger.setSeatNumber(seatNumber);
        passenger.setCheckInGroup(checkInGroup);
        try {
            return passengerRepository.save(passenger);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not update passenger with id = " + id, e);
        }
    }

    public void incrementReservedSeatsCount(String seatClass, Flight flight) {
        switch (seatClass) {
            case FIRST_CLASS: {
                Integer reservedSeatsCount = flight
                        .getReservedFirstClassSeatsCount();
                Integer maxSeatsCount = flight.getAirplane()
                        .getMaxFirstClassSeatsCount();
                Integer availableSeatsCount = maxSeatsCount
                        - reservedSeatsCount;
                if (availableSeatsCount > 0) {
                    flight.setReservedFirstClassSeatsCount(
                            reservedSeatsCount + 1);
                } else if (availableSeatsCount == 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "There are 0 available first class seats.");
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The flight is overbooked for first class.");
                }
                break;
            }
            case BUSINESS_CLASS: {
                Integer reservedSeatsCount = flight
                        .getReservedBusinessClassSeatsCount();
                Integer maxSeatsCount = flight.getAirplane()
                        .getMaxBusinessClassSeatsCount();
                Integer availableSeatsCount = maxSeatsCount
                        - reservedSeatsCount;
                if (availableSeatsCount > 0) {
                    flight.setReservedBusinessClassSeatsCount(
                            reservedSeatsCount + 1);
                } else if (availableSeatsCount == 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "There are 0 available business class seats.");
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The flight is overbooked for business class.");
                }
                break;
            }
            case ECONOMY_CLASS: {
                Integer reservedSeatsCount = flight
                        .getReservedEconomyClassSeatsCount();
                Integer maxSeatsCount = flight.getAirplane()
                        .getMaxEconomyClassSeatsCount();
                Integer availableSeatsCount = maxSeatsCount
                        - reservedSeatsCount;
                if (availableSeatsCount > 0) {
                    flight.setReservedEconomyClassSeatsCount(
                            reservedSeatsCount + 1);
                } else if (availableSeatsCount == 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "There are 0 available economy class seats.");
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The flight is overbooked for economy class.");
                }
                break;
            }
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid seat class: " + seatClass);
        }
    }

    public void decrementReservedSeatsCount(String seatClass, Flight flight) {
        switch (seatClass) {
            case FIRST_CLASS: {
                Integer reservedSeatsCount = flight
                        .getReservedFirstClassSeatsCount();
                if (reservedSeatsCount > 0) {
                    flight.setReservedFirstClassSeatsCount(
                            reservedSeatsCount - 1);
                } else {
                    String reason = String.format(
                            "There are %s reserved seats in first class in flight with id=%s",
                            reservedSeatsCount, flight.getId());
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            reason);
                }
                break;
            }
            case BUSINESS_CLASS: {
                Integer reservedSeatsCount = flight
                        .getReservedBusinessClassSeatsCount();
                if (reservedSeatsCount > 0) {
                    flight.setReservedBusinessClassSeatsCount(
                            reservedSeatsCount - 1);
                } else {
                    String reason = String.format(
                            "There are %s reserved seats in business class.",
                            reservedSeatsCount);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            reason);
                }
                break;
            }
            case ECONOMY_CLASS: {
                Integer reservedSeatsCount = flight
                        .getReservedEconomyClassSeatsCount();
                if (reservedSeatsCount > 0) {
                    flight.setReservedEconomyClassSeatsCount(
                            reservedSeatsCount - 1);
                } else {
                    String reason = String.format(
                            "There are %s reserved seats in economy class",
                            reservedSeatsCount);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            reason);
                }
                break;
            }
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid seat class: " + seatClass);
        }
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

    public List<Passenger> getPassengersByFlightId(Long flightId) {
        return passengerRepository.findByFlightId(flightId);
    }

    public List<Integer> getTakenSeats(Long flightId) {
        List<Passenger> passengers = getPassengersByFlightId(flightId);
        List<Integer> seatList = new ArrayList<Integer>();
        for (Passenger p: passengers) {
            seatList.add(p.getSeatNumber());
        }
       return seatList;
    }
}
