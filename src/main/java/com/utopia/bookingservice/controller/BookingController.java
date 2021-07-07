package com.utopia.bookingservice.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.utopia.bookingservice.dto.BookingCreationDto;
import com.utopia.bookingservice.dto.BookingDto;
import com.utopia.bookingservice.dto.BookingUpdateDto;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.exception.ModelMapperFailedException;
import com.utopia.bookingservice.propertymap.BookingCreationDtoMap;
import com.utopia.bookingservice.propertymap.FlightMap;
import com.utopia.bookingservice.security.JwtUtils;
import com.utopia.bookingservice.service.BookingService;
import com.utopia.bookingservice.util.DtoConverter;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@RestController
@SecurityScheme(name = "bearer", // can be set to anything
        type = SecuritySchemeType.HTTP, scheme = "bearer")
@OpenAPIDefinition(info = @Info(title = "Booking Service", version = "v1"),
        security = @SecurityRequirement(name = "bearer"))
public class BookingController {
    private static final Double CHECK_IN_GROUP_UPGRADE_PRICE = 50d;
    private static final Double SEAT_CLASS_UPGRADE_RATE = 0.15;

    private final BookingService bookingService;
    private final ModelMapper modelMapper;
    private final DtoConverter dtoConverter;

    @Value("${jwt.secret-key}")
    private String jwtSecretKey;

    public BookingController(BookingService bookingService,
            ModelMapper modelMapper, DtoConverter dtoConverter) {
        this.bookingService = bookingService;
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("bookings")
    public ResponseEntity<List<BookingDto>> findAllBookings() {
        List<Booking> bookings = bookingService.findAllBookings();
        List<BookingDto> bookingDtos = bookings.stream()
                .map(dtoConverter::convertBookingToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDtos);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("bookings/{confirmation_code}")
    public ResponseEntity<BookingDto> findByConfirmationCode(
            @PathVariable("confirmation_code") String confirmationCode) {
        final Booking booking = bookingService
                .findByConfirmationCode(confirmationCode);
        final BookingDto bookingDto = this.convertToDto(booking);
        return ResponseEntity.ok(bookingDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("bookings/search")
    public ResponseEntity<List<BookingDto>> findBookingsByModelContaining(
            @RequestParam("confirmation_code") String confirmationCode) {
        List<Booking> bookings = bookingService
                .findByConfirmationCodeContaining(confirmationCode);
        List<BookingDto> bookingDtos = bookings.stream().map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDtos);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("bookings/username/{username}")
    public ResponseEntity<Page<BookingDto>> findByUsername(
            @PathVariable("username") String username,
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize,
            @RequestHeader("authorization") String bearerToken) {
        checkUsernameRequestMatchesResponse(bearerToken, username);
        Page<Booking> bookingsPage = bookingService.findByUsername(username,
                pageIndex, pageSize);
        Page<BookingDto> bookingDtosPage = bookingsPage
                .map((Booking booking) -> modelMapper.map(booking,
                        BookingDto.class));
        return ResponseEntity.ok(bookingDtosPage);
    }

    private void checkUsernameRequestMatchesResponse(String bearerToken,
            String responseUsername) throws ResponseStatusException {
        String username = JwtUtils.getUsernameFromToken(bearerToken,
                jwtSecretKey);
        String role = JwtUtils.getRoleFromToken(bearerToken, jwtSecretKey);

        // if the user who sent the request is not an admin, then they can't
        // view other users' information
        // they can only view and alter their own
        if (!role.contains("ADMIN") && !username.equals(responseUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only admins can access another user's information.");
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @PostMapping("bookings")
    public ResponseEntity<BookingDto> create(
            @Valid @RequestBody BookingCreationDto bookingCreationDto,
            UriComponentsBuilder builder) {
        Booking bookingToCreate = modelMapper.map(bookingCreationDto,
                Booking.class);

        Booking createdBooking = bookingService.create(bookingToCreate,
                bookingCreationDto.getUsername());
        BookingDto createdBookingDto = convertToDto(createdBooking);
        return ResponseEntity
                .created(builder.path("/bookings/{id}")
                        .build(createdBookingDto.getId()))
                .body(createdBookingDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("bookings/{id}")
    public ResponseEntity<BookingDto> update(@PathVariable Long id,
            @Valid @RequestBody BookingUpdateDto bookingUpdateDto)
            throws ModelMapperFailedException {
        Booking targetBooking;
        targetBooking = modelMapper.map(bookingUpdateDto, Booking.class);
        String username = bookingUpdateDto.getUsername();
        Booking updatedBooking = bookingService.update(id, username,
                targetBooking);
        BookingDto updatedBookingDto = convertToDto(updatedBooking);
        return ResponseEntity.ok(updatedBookingDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
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
