package com.utopia.bookingservice.controller;

import java.util.Map;

import javax.validation.Valid;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.utopia.bookingservice.dto.BookingCreationDto;
import com.utopia.bookingservice.dto.BookingResponseDto;
import com.utopia.bookingservice.dto.BookingUpdateDto;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.exception.ModelMapperFailedException;
import com.utopia.bookingservice.propertymap.BookingCreationDtoMap;
import com.utopia.bookingservice.propertymap.BookingMap;
import com.utopia.bookingservice.propertymap.FlightMap;
import com.utopia.bookingservice.security.JwtUtils;
import com.utopia.bookingservice.service.BookingService;

import org.modelmapper.ModelMapper;
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

    @Value("${jwt.secret-key}")
    private String jwtSecretKey;

    public BookingController(BookingService bookingService,
            ModelMapper modelMapper) {
        this.bookingService = bookingService;
        this.modelMapper = modelMapper;
        this.modelMapper.addMappings(new BookingMap());
        this.modelMapper.addMappings(new BookingCreationDtoMap());
        this.modelMapper.addMappings(new FlightMap());
    }

    @GetMapping
    public @ResponseBody ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("Health is OK.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("bookings")
    public ResponseEntity<Page<BookingResponseDto>> findAll(
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize) {
        final Page<Booking> bookingsPage = bookingService.findAll(pageIndex,
                pageSize);
        final Page<BookingResponseDto> bookingDtosPage = bookingsPage
                .map(this::convertToResponseDto);
        return ResponseEntity.ok(bookingDtosPage);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("bookings/{confirmation_code}")
    public ResponseEntity<BookingResponseDto> findByConfirmationCode(
            @PathVariable("confirmation_code") String confirmationCode) {
        final Booking booking = bookingService
                .findByConfirmationCode(confirmationCode);
        final BookingResponseDto bookingDto = this.convertToResponseDto(booking);
        return ResponseEntity.ok(bookingDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("bookings/search")
    public ResponseEntity<Page<BookingResponseDto>> findBookingsByConfirmationCodeContaining(
            @RequestParam("confirmation_code") String confirmationCode,
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize) {
        Page<Booking> bookingsPage = bookingService
                .findByConfirmationCodeContaining(confirmationCode, pageIndex,
                        pageSize);
        Page<BookingResponseDto> bookingDtosPage = bookingsPage
                .map(this::convertToResponseDto);
        return ResponseEntity.ok(bookingDtosPage);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("bookings/username/{username}")
    public ResponseEntity<Page<BookingResponseDto>> findByUsername(
            @PathVariable("username") String username,
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize,
            @RequestHeader("Authorization") String bearerToken) {
        checkUsernameRequestMatchesResponse(bearerToken, username);
        Page<Booking> bookingsPage = bookingService.findByUsername(username,
                pageIndex, pageSize);
        Page<BookingResponseDto> bookingDtosPage = bookingsPage
                .map(this::convertToResponseDto);
        return ResponseEntity.ok(bookingDtosPage);
    }

    private void checkUsernameRequestMatchesResponse(String bearerToken,
            String responseUsername) throws ResponseStatusException {
        // String username = JwtUtils.getUsernameFromToken(bearerToken,
        //         jwtSecretKey);
        // String role = JwtUtils.getRoleFromToken(bearerToken, jwtSecretKey);

        // if (!role.contains("ADMIN") && !username.equals(responseUsername)) {
        //     throw new ResponseStatusException(HttpStatus.FORBIDDEN,
        //             "Only admins can access another user's information.");
        // }
    }

    private void checkUsernameRequestMatchesResponse2(String bearerToken,
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
    public ResponseEntity<BookingResponseDto> create(
            @Valid @RequestBody BookingCreationDto bookingCreationDto,
            UriComponentsBuilder builder) {
        Booking bookingToCreate = modelMapper.map(bookingCreationDto,
                Booking.class);

        Booking createdBooking = bookingService
                .create(bookingCreationDto.getUsername(), bookingToCreate);
        BookingResponseDto createdBookingDto = convertToResponseDto(createdBooking);
        return ResponseEntity
                .created(builder.path("/bookings/{id}")
                        .build(createdBookingDto.getId()))
                .body(createdBookingDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("bookings/{id}")
    public ResponseEntity<BookingResponseDto> update(@PathVariable Long id,
            @Valid @RequestBody BookingUpdateDto bookingUpdateDto)
            throws ModelMapperFailedException {
        Booking targetBooking;
        targetBooking = modelMapper.map(bookingUpdateDto, Booking.class);
        Booking updatedBooking = bookingService.update(id, targetBooking);
        BookingResponseDto updatedBookingDto = convertToResponseDto(updatedBooking);
        return ResponseEntity.ok(updatedBookingDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @DeleteMapping("bookings/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id)
            throws ModelMapperFailedException {
        bookingService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private BookingResponseDto convertToResponseDto(Booking booking) {
        return modelMapper.map(booking, BookingResponseDto.class);
    }
}
