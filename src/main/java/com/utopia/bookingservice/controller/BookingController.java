package com.utopia.bookingservice.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.utopia.bookingservice.dto.BookingCreationDto;
import com.utopia.bookingservice.dto.BookingDto;
import com.utopia.bookingservice.dto.BookingUpdateDto;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.User;
import com.utopia.bookingservice.exception.ModelMapperFailedException;
import com.utopia.bookingservice.propertymap.BookingCreationDtoMap;
import com.utopia.bookingservice.propertymap.FlightMap;
import com.utopia.bookingservice.service.BookingService;
import com.utopia.bookingservice.service.UserService;
import com.utopia.bookingservice.util.DtoConverter;

import org.hibernate.MappingException;
import org.modelmapper.ConfigurationException;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.data.domain.Page;
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
    private static final Double CHECK_IN_GROUP_UPGRADE_PRICE = 50d;
    private static final Double SEAT_CLASS_UPGRADE_RATE = 0.15;

    private final BookingService bookingService;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final DtoConverter dtoConverter;

    public BookingController(BookingService bookingService,
            UserService userService, ModelMapper modelMapper,
            DtoConverter dtoConverter) {
        this.bookingService = bookingService;
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
        this.modelMapper
                .addMappings(new PropertyMap<BookingUpdateDto, Booking>() {
                    @Override
                    protected void configure() {
                        map().getUser().setUsername(source.getUsername());
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

    @GetMapping("bookings/username/{username}")
    public ResponseEntity<Page<BookingDto>> findByUsername(
            @PathVariable("username") String username,
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize) {
        Page<Booking> bookingsPage = bookingService.findByUsername(username,
                pageIndex, pageSize);
        Page<BookingDto> bookingDtosPage = bookingsPage
                .map((Booking booking) -> modelMapper.map(booking,
                        BookingDto.class));
        return ResponseEntity.ok(bookingDtosPage);
    }

    @PostMapping("bookings")
    public ResponseEntity<BookingDto> createBooking(
            @Valid @RequestBody BookingCreationDto bookingCreationDto,
            UriComponentsBuilder builder) {
        Booking creatingBooking = modelMapper.map(bookingCreationDto,
                Booking.class);

        User user = userService
                .findByUsername(bookingCreationDto.getUsername());
        creatingBooking.setUser(user);

        creatingBooking.setTotalPrice(0d);
        Booking newBooking = bookingService.create(creatingBooking);
        BookingDto newBookingDto = convertToDto(newBooking);
        return ResponseEntity.created(
                builder.path("/bookings/{id}").build(newBooking.getId()))
                .body(newBookingDto);
    }

    @PutMapping("bookings/{id}")
    public ResponseEntity<BookingDto> updateBooking(@PathVariable Long id,
            @Valid @RequestBody BookingUpdateDto bookingUpdateDto)
            throws ModelMapperFailedException {
        Booking updatingBooking;
        try {
            updatingBooking = modelMapper.map(bookingUpdateDto, Booking.class);
        } catch (IllegalArgumentException | ConfigurationException
                | MappingException e) {
            throw new ModelMapperFailedException(e);
        }
        User user = userService.findByUsername(bookingUpdateDto.getUsername());
        updatingBooking.setUser(user);
        Booking updatedBooking = bookingService.updateBooking(id,
                updatingBooking);
        return ResponseEntity.ok(convertToDto(updatedBooking));
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

    private Booking convertToEntity(BookingDto bookingDto) {
        return modelMapper.map(bookingDto, Booking.class);
    }
}
