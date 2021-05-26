package com.utopia.bookingservice.controller;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import com.utopia.bookingservice.dto.BookingDto;
import com.utopia.bookingservice.dto.FlightDto;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Flight;
import com.utopia.bookingservice.exception.ModelMapperFailedException;
import com.utopia.bookingservice.propertymap.PassengerMap;
import com.utopia.bookingservice.service.BookingService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final ModelMapper modelMapper;
    private final DtoConverter dtoConverter;

    public BookingController(BookingService bookingService,
            ModelMapper modelMapper, DtoConverter dtoConverter) {
        this.bookingService = bookingService;
        this.dtoConverter = dtoConverter;
        this.modelMapper = modelMapper;
        this.modelMapper.addMappings(new PropertyMap<Booking, BookingDto>() {
            @Override
            protected void configure() {
                map().setUsername(source.getUser().getUsername());
            }
        });
        this.modelMapper.addMappings(new PropertyMap<Flight, FlightDto>() {
            @Override
            protected void configure() {
                map().setRouteId(source.getRoute().getId());
                map().setRouteActive(source.getRoute().getActive());
                map().setOriginAirportCode(
                        source.getRoute().getOriginAirport().getIataId());
                map().setOriginAirportCity(
                        source.getRoute().getOriginAirport().getCity());
                map().setOriginAirportActive(
                        source.getRoute().getOriginAirport().getActive());
                map().setDestinationAirportCode(
                        source.getRoute().getDestinationAirport().getIataId());
                map().setDestinationAirportCity(
                        source.getRoute().getDestinationAirport().getCity());
                map().setDestinationAirportActive(
                        source.getRoute().getDestinationAirport().getActive());
            }
        });
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> findAllBookings() {
        List<Booking> bookings = bookingService.findAllBookings();
        List<BookingDto> bookingDtos = bookings.stream()
                .map(dtoConverter::convertBookingToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDtos);
    }

    @GetMapping("{confirmation_code}")
    public ResponseEntity<BookingDto> findByConfirmationCode(
            @PathVariable("confirmation_code") String confirmationCode) {
        final Booking booking = bookingService
                .findByConfirmationCode(confirmationCode);
        final BookingDto bookingDto = this.convertToDto(booking);
        return ResponseEntity.ok(bookingDto);
    }

    @GetMapping("search")
    public ResponseEntity<List<BookingDto>> findBookingsByModelContaining(
            @RequestParam("confirmation_code") String confirmationCode) {
        List<Booking> bookings = bookingService
                .findByConfirmationCodeContaining(confirmationCode);
        List<BookingDto> bookingDtos = bookings.stream().map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDtos);
    }

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(
            @RequestBody BookingDto bookingDto, UriComponentsBuilder builder) {
        Booking booking;
        try {
            booking = convertToEntity(bookingDto);
        } catch (ParseException e) {
            throw new ModelMapperFailedException(e);
        }
        Booking createdBooking = bookingService.createBooking(booking);
        return ResponseEntity
                .created(builder.path("/bookings/{id}")
                        .build(bookingDto.getId()))
                .body(convertToDto(createdBooking));
    }

    @PutMapping("{id}")
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

    @DeleteMapping("{id}")
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
