package com.utopia.bookingservice.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import javax.validation.Valid;

import com.utopia.bookingservice.dto.PassengerCreationDto;
import com.utopia.bookingservice.dto.PassengerDto;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Discount;
import com.utopia.bookingservice.entity.Flight;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.propertymap.CreatingPassengerDtoMap;
import com.utopia.bookingservice.propertymap.PassengerDtoMap;
import com.utopia.bookingservice.propertymap.PassengerMap;
import com.utopia.bookingservice.service.BookingService;
import com.utopia.bookingservice.service.DiscountService;
import com.utopia.bookingservice.service.FlightService;
import com.utopia.bookingservice.service.PassengerService;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("passengers")
public class PassengerController {
    private static final Integer childDiscountAge = 2;
    private static final Integer edlerDiscountAge = 65;
    private static final Double layoverDiscountRate = 0.1;

    private final PassengerService passengerService;
    private final BookingService bookingService;
    private final FlightService flightService;
    private final DiscountService discountService;
    private final ModelMapper modelMapper;

    public PassengerController(PassengerService passengerService,
            BookingService bookingService, FlightService flightService,
            DiscountService discountService, ModelMapper modelMapper) {
        this.passengerService = passengerService;
        this.bookingService = bookingService;
        this.flightService = flightService;
        this.discountService = discountService;
        this.modelMapper = modelMapper;
        this.modelMapper.addMappings(new PassengerMap());
        this.modelMapper.addMappings(new PassengerDtoMap());
        this.modelMapper.addMappings(new CreatingPassengerDtoMap());
    }

    @GetMapping
    public ResponseEntity<Page<PassengerDto>> findAll(
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize) {
        final Page<Passenger> passengers = passengerService.findAll(pageIndex,
                pageSize);
        final Page<PassengerDto> passengerDtos = passengers
                .map(this::convertPassengerToDto);
        return ResponseEntity.ok(passengerDtos);
    }

    @GetMapping("{id}")
    public ResponseEntity<PassengerDto> findById(@PathVariable Long id) {
        final Passenger passenger = passengerService.findById(id);
        final PassengerDto passengerDto = this.convertPassengerToDto(passenger);
        return ResponseEntity.ok(passengerDto);
    }

    @GetMapping("search")
    public ResponseEntity<Page<PassengerDto>> findByConfirmationCodeOrUsernameContaining(
            @RequestParam("term") String searchTerm,
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize) {
        Page<Passenger> passengers = passengerService
                .findByConfirmationCodeOrUsernameContaining(searchTerm,
                        pageIndex, pageSize);
        Page<PassengerDto> passengerDtos = passengers
                .map(this::convertPassengerToDto);
        return ResponseEntity.ok(passengerDtos);
    }

    @GetMapping("distinct_search")
    public ResponseEntity<Page<PassengerDto>> findDistinctByConfirmationCodeOrUsernameContaining(
            @RequestParam("term") String searchTerm,
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize) {
        Page<Passenger> passengers = passengerService
                .findDistinctByConfirmationCodeOrUsernameContaining(searchTerm,
                        pageIndex, pageSize);
        Page<PassengerDto> passengerDtos = passengers
                .map(this::convertPassengerToDto);
        return ResponseEntity.ok(passengerDtos);
    }

    @PostMapping
    public ResponseEntity<PassengerDto> create(
            @Valid @RequestBody PassengerCreationDto passengerCreationDto,
            UriComponentsBuilder builder) {
        Passenger creatingPassenger = modelMapper.map(passengerCreationDto,
                Passenger.class);

        String originAirportCode = passengerCreationDto.getOriginAirportCode();
        String destinationAirportCode = passengerCreationDto
                .getDestinationAirportCode();
        String airplaneModel = passengerCreationDto.getAirplaneModel();
        LocalDateTime departureTime = passengerCreationDto.getDepartureTime();
        LocalDateTime arrivalTime = passengerCreationDto.getArrivalTime();
        Flight flight = flightService.identifyFlight(originAirportCode,
                destinationAirportCode, airplaneModel, departureTime,
                arrivalTime);
        creatingPassenger.setFlight(flight);

        Double basePrice = 0d;
        if (passengerCreationDto.getSeatClass() == "first") {
            basePrice = flight.getFirstClassPrice();
        } else if (passengerCreationDto.getSeatClass() == "business") {
            basePrice = flight.getBusinessClassPrice();
        } else if (passengerCreationDto.getSeatClass() == "economy") {
            basePrice = flight.getEconomyClassPrice();
        }
        Integer age = Period
                .between(passengerCreationDto.getDateOfBirth(), LocalDate.now())
                .getYears();
        Discount discount;
        if (age <= 2) {
            discount = discountService.findByDiscountType("child");
        } else if (age >= 65) {
            discount = discountService.findByDiscountType("elderly");
        } else {
            discount = discountService.findByDiscountType("none");
        }
        creatingPassenger.setDiscount(discount);
        Double discountRate = discount.getDiscountRate();
        Booking booking = bookingService.findByConfirmationCode(
                creatingPassenger.getBooking().getConfirmationCode());
        Integer layoverCount = booking.getLayoverCount();
        booking.setTotalPrice(booking.getTotalPrice()
                + calculateTotalPrice(basePrice, discountRate, layoverCount));
        creatingPassenger.setBooking(booking);

        Passenger createdPassenger = passengerService.create(creatingPassenger);
        PassengerDto createdPassengerDto = modelMapper.map(createdPassenger,
                PassengerDto.class);
        return ResponseEntity
                .created(builder.path("/passengers/{id}")
                        .build(createdPassengerDto.getId()))
                .body(createdPassengerDto);
    }

    private Double calculateTotalPrice(Double basePrice, Double discountRate,
            Integer layoverCount) {
        if (layoverCount > 0) {
            discountRate += layoverDiscountRate;
        }
        return basePrice * discountRate;
    }

    @PutMapping("{id}")
    public ResponseEntity<PassengerDto> update(@PathVariable Long id,
            @Valid @RequestBody PassengerDto passengerDto,
            UriComponentsBuilder builder) {
        passengerDto.setId(id);
        Passenger passenger;
        passenger = convertDtoToPassenger(passengerDto);
        Passenger updatedPassenger = passengerService.update(passenger);
        return ResponseEntity.ok(convertPassengerToDto(updatedPassenger));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        passengerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private PassengerDto convertPassengerToDto(Passenger passenger) {
        return modelMapper.map(passenger, PassengerDto.class);
    }

    private Passenger convertDtoToPassenger(PassengerDto passengerDto) {
        return modelMapper.map(passengerDto, Passenger.class);
    }
}
