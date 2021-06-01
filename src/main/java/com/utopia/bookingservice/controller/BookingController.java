package com.utopia.bookingservice.controller;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.utopia.bookingservice.dto.BookingDto;
import com.utopia.bookingservice.dto.CreatingBookingDto;
import com.utopia.bookingservice.dto.FlightDto;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Discount;
import com.utopia.bookingservice.entity.Flight;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.entity.User;
import com.utopia.bookingservice.exception.ModelMapperFailedException;
import com.utopia.bookingservice.propertymap.CreatingBookingDtoMap;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class BookingController {
    private static final Integer childDiscountAge = 2;
    private static final Integer edlerDiscountAge = 2;

    private final BookingService bookingService;
    private final FlightService flightService;
    private final PassengerService passengerService;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final DtoConverter dtoConverter;

    public BookingController(BookingService bookingService,
            FlightService flightService, PassengerService passengerService, UserService userService,
            ModelMapper modelMapper, DtoConverter dtoConverter) {
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
        this.modelMapper.addMappings(new CreatingBookingDtoMap());
        this.modelMapper.addMappings(new PropertyMap<Flight, FlightDto>() {
            @Override
            protected void configure() {
                map().setRouteId(source.getRoute().getId());
                map().setRouteActive(source.getRoute().getActive());

                map().setOriginAirportCode(
                        source.getRoute().getOriginAirport().getAirportCode());
                map().setOriginAirportCity(
                        source.getRoute().getOriginAirport().getCity());
                map().setOriginAirportActive(
                        source.getRoute().getOriginAirport().getActive());
                map().setDestinationAirportCode(source.getRoute()
                        .getDestinationAirport().getAirportCode());
                map().setDestinationAirportCity(
                        source.getRoute().getDestinationAirport().getCity());
                map().setDestinationAirportActive(
                        source.getRoute().getDestinationAirport().getActive());

                map().setAirplaneModel(source.getAirplane().getModel());
            }
        });
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
            @Valid @RequestBody CreatingBookingDto creatingBookingDto,
            UriComponentsBuilder builder) {
        Booking creatingBooking = modelMapper.map(creatingBookingDto,
                Booking.class);

        Passenger creatingPassenger = new Passenger();
        String originAirportCode = creatingBookingDto.getOriginAirportCode();
        String destinationAirportCode = creatingBookingDto
                .getDestinationAirportCode();
        String airplaneModel = creatingBookingDto.getAirplaneModel();
        LocalDateTime departureTime = creatingBookingDto.getDepartureTime();
        LocalDateTime arrivalTime = creatingBookingDto.getArrivalTime();
        Flight flight = flightService.identifyFlight(originAirportCode,
                destinationAirportCode, airplaneModel, departureTime,
                arrivalTime);
        creatingPassenger.setFlight(flight);
        Discount discount = new Discount();
        discount.setDiscountType(creatingBookingDto.getDiscountType());
        creatingPassenger.setDiscount(discount);
        creatingPassenger.setGivenName(creatingBookingDto.getGivenName());
        creatingPassenger.setFamilyName(creatingBookingDto.getFamilyName());
        creatingPassenger.setDateOfBirth(creatingBookingDto.getDateOfBirth());
        creatingPassenger.setGender(creatingBookingDto.getGender());
        creatingPassenger.setAddress(creatingBookingDto.getAddress());
        creatingPassenger.setSeatClass(creatingBookingDto.getSeatClass());
        creatingPassenger.setSeatNumber(creatingBookingDto.getSeatNumber());
        creatingPassenger.setCheckInGroup(creatingBookingDto.getCheckInGroup());

        User user = userService.findByUsername(creatingBookingDto.getUsername());
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
