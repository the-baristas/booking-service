package com.utopia.bookingservice.controller;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.utopia.bookingservice.dto.BookingCreationDto;
import com.utopia.bookingservice.dto.BookingDto;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Discount;
import com.utopia.bookingservice.entity.Flight;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.entity.User;
import com.utopia.bookingservice.exception.ModelMapperFailedException;
import com.utopia.bookingservice.propertymap.BookingCreationDtoMap;
import com.utopia.bookingservice.propertymap.FlightMap;
import com.utopia.bookingservice.service.BookingService;
import com.utopia.bookingservice.service.FlightService;
import com.utopia.bookingservice.service.PassengerService;
import com.utopia.bookingservice.service.UserService;
import com.utopia.bookingservice.util.DtoConverter;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class BookingController {
    private static final Integer childDiscountAge = 2;
    private static final Integer edlerDiscountAge = 65;
    private static final Double checkInGroupUpgradePrice = 50d;
    private static final Double classUpgradeRate = 0.15;
    private static final Double layoverDiscountRate = 0.1;

    private final BookingService bookingService;
    private final FlightService flightService;
    private final PassengerService passengerService;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final DtoConverter dtoConverter;

    public BookingController(BookingService bookingService,
            FlightService flightService, PassengerService passengerService,
            UserService userService, ModelMapper modelMapper,
            DtoConverter dtoConverter) {
        this.bookingService = bookingService;
        this.flightService = flightService;
        this.passengerService = passengerService;
        this.userService = userService;
        this.dtoConverter = dtoConverter;
        this.modelMapper = modelMapper;
        this.modelMapper.addMappings(new PropertyMap<Booking, BookingDto>() {
            @Override
            protected void configure() {
                map().setUsername(source.getUser().getUsername());
                map().setEmail(source.getUser().getEmail());
                map().setPhone(source.getUser().getPhone());
            }
        });
        this.modelMapper.addMappings(new BookingCreationDtoMap());
        this.modelMapper.addMappings(new FlightMap());
    }

    @GetMapping("/")
    public @ResponseBody ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("Health is OK.");
    }

    @GetMapping("bookings")
    public ResponseEntity<List<BookingDto>> findAllBookings() {
        List<Booking> bookings = bookingService.findAllBookings();
        List<BookingDto> bookingDtos = bookings.stream()
                .map(dtoConverter::convertBookingToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDtos);
    }

    @GetMapping("bookings/{confirmation_code}")
    public ResponseEntity<BookingDto> findByConfirmationCode(
            @PathVariable("confirmation_code") String confirmationCode) {
        final Booking booking = bookingService
                .findByConfirmationCode(confirmationCode);
        final BookingDto bookingDto = this.convertToDto(booking);
        return ResponseEntity.ok(bookingDto);
    }

    @GetMapping("bookings/search")
    public ResponseEntity<List<BookingDto>> findBookingsByModelContaining(
            @RequestParam("confirmation_code") String confirmationCode) {
        List<Booking> bookings = bookingService
                .findByConfirmationCodeContaining(confirmationCode);
        List<BookingDto> bookingDtos = bookings.stream().map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDtos);
    }

    @PostMapping("bookings")
    public ResponseEntity<BookingDto> createBooking(
            @Valid @RequestBody BookingCreationDto bookingCreationDto,
            UriComponentsBuilder builder) {
        Booking creatingBooking = modelMapper.map(bookingCreationDto,
                Booking.class);

        Passenger creatingPassenger = new Passenger();
        String originAirportCode = bookingCreationDto.getOriginAirportCode();
        String destinationAirportCode = bookingCreationDto
                .getDestinationAirportCode();
        String airplaneModel = bookingCreationDto.getAirplaneModel();
        LocalDateTime departureTime = bookingCreationDto.getDepartureTime();
        LocalDateTime arrivalTime = bookingCreationDto.getArrivalTime();

        Flight flight = flightService.identifyFlight(originAirportCode,
                destinationAirportCode, airplaneModel, departureTime,
                arrivalTime);
        creatingPassenger.setFlight(flight);

        Discount discount = new Discount();
        discount.setDiscountType(bookingCreationDto.getDiscountType());

        creatingPassenger.setDiscount(discount);
        creatingPassenger.setGivenName(bookingCreationDto.getGivenName());
        creatingPassenger.setFamilyName(bookingCreationDto.getFamilyName());
        creatingPassenger.setDateOfBirth(bookingCreationDto.getDateOfBirth());
        creatingPassenger.setGender(bookingCreationDto.getGender());
        creatingPassenger.setAddress(bookingCreationDto.getAddress());
        creatingPassenger.setSeatClass(bookingCreationDto.getSeatClass());
        creatingPassenger.setSeatNumber(bookingCreationDto.getSeatNumber());
        creatingPassenger.setCheckInGroup(bookingCreationDto.getCheckInGroup());

        User user = userService
                .findByUsername(bookingCreationDto.getUsername());
        creatingBooking.setUser(user);

        Booking newBooking = bookingService.create(creatingBooking);
        creatingPassenger.setBooking(newBooking);

        passengerService.create(creatingPassenger);
        return ResponseEntity
                .created(builder.path("/bookings/{id}")
                        .build(newBooking.getId()))
                .body(convertToDto(newBooking));
    }

    @PutMapping("bookings/{id}")
    public ResponseEntity<BookingDto> updateBooking(@PathVariable Long id,
            @RequestBody BookingDto bookingDto)
            throws ModelMapperFailedException {
        Booking booking;
        try {
            booking = convertToEntity(bookingDto);
        } catch (ParseException e) {
            throw new ModelMapperFailedException(e);
        }
        Booking updateBooking = bookingService.updateBooking(id, booking);
        return ResponseEntity.ok(convertToDto(updateBooking));
    }

    @DeleteMapping("bookings/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id)
            throws ModelMapperFailedException {
        bookingService.deleteBookingById(id);
        return ResponseEntity.noContent().build();
    }

    private BookingDto convertToDto(Booking booking) {
        return modelMapper.map(booking, BookingDto.class);
    }

    private Booking convertToEntity(BookingDto bookingDto)
            throws ParseException {
        return modelMapper.map(bookingDto, Booking.class);
    }
}
