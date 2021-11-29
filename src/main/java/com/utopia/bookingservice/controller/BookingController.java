package com.utopia.bookingservice.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.Valid;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.stripe.exception.StripeException;
import com.utopia.bookingservice.dto.*;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Discount;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.exception.ModelMapperFailedException;
import com.utopia.bookingservice.propertymap.BookingCreationDtoMap;
import com.utopia.bookingservice.propertymap.BookingMap;
import com.utopia.bookingservice.propertymap.FlightMap;
import com.utopia.bookingservice.security.JwtUtils;
import com.utopia.bookingservice.service.BookingService;

import com.utopia.bookingservice.service.FlightService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final FlightService flightService;
    private final ModelMapper modelMapper;

    @Value("${jwt.secret-key}")
    private String jwtSecretKey;

    public BookingController(BookingService bookingService, FlightService flightService,
            ModelMapper modelMapper) {
        this.bookingService = bookingService;
        this.flightService = flightService;
        this.modelMapper = modelMapper;
        this.modelMapper.addMappings(new BookingMap());
        this.modelMapper.addMappings(new BookingCreationDtoMap());
        this.modelMapper.addMappings(new FlightMap());
    }

    @GetMapping
    public ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("Health is OK.");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
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

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @GetMapping("bookings/{confirmation_code}")
    public ResponseEntity<BookingResponseDto> findByConfirmationCode(
            @PathVariable("confirmation_code") String confirmationCode) {
        final Booking booking = bookingService
                .findByConfirmationCode(confirmationCode);
        final BookingResponseDto bookingDto = this
                .convertToResponseDto(booking);
        return ResponseEntity.ok(bookingDto);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @GetMapping("bookings/search")
    public ResponseEntity<Page<BookingResponseDto>> findByConfirmationCodeContaining(
            @RequestParam("term") String searchTerm,
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize) {
        Page<Booking> bookingsPage = bookingService
                .findByConfirmationCodeContaining(searchTerm, pageIndex,
                        pageSize);
        Page<BookingResponseDto> bookingDtosPage = bookingsPage
                .map(this::convertToResponseDto);
        return ResponseEntity.ok(bookingDtosPage);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_AGENT')")
    @GetMapping("bookings/username/{username}")
    public ResponseEntity<Page<BookingResponseDto>> findByUsername(
            @PathVariable("username") String username,
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize,
            @RequestParam(name = "pendingOnly",
                    defaultValue = "false") Boolean pendingOnly,
            @RequestParam(name = "term", defaultValue = "") String searchTerm,
            @RequestHeader("Authorization") String bearerToken) {
        checkUsernameRequestMatchesResponse(bearerToken, username);

        Page<Booking> bookingsPage;

        if (!pendingOnly) {
            if (searchTerm.equals(""))
                bookingsPage = bookingService.findByUsername(username,
                        pageIndex, pageSize);
            else
                bookingsPage = bookingService.findByUsername(username,
                        searchTerm, pageIndex, pageSize);
        } else {
            if (searchTerm.equals(""))
                bookingsPage = bookingService.findPendingFlightsByUsername(
                        username, pageIndex, pageSize);
            else
                bookingsPage = bookingService.findPendingFlightsByUsername(
                        username, searchTerm, pageIndex, pageSize);
        }

        return ResponseEntity.ok(bookingsPage.map(this::convertToResponseDto));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_AGENT')")
    @PostMapping("bookings")
    public ResponseEntity<BookingResponseDto> create(
            @Valid @RequestBody BookingCreationDto bookingCreationDto,
            UriComponentsBuilder builder) {
        Booking bookingToCreate = modelMapper.map(bookingCreationDto,
                Booking.class);

        Booking createdBooking = bookingService
                .create(bookingCreationDto.getUsername(), bookingToCreate);
        BookingResponseDto createdBookingDto = convertToResponseDto(
                createdBooking);
        Long id = createdBookingDto.getId();
        return ResponseEntity.created(builder.path("/bookings/{id}").build(id))
                .body(createdBookingDto);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_AGENT')")
    @PutMapping("bookings/{id}")
    public ResponseEntity<BookingResponseDto> update(@PathVariable Long id,
            @Valid @RequestBody BookingUpdateDto bookingUpdateDto) {
        Booking targetBooking = modelMapper.map(bookingUpdateDto,
                Booking.class);
        Booking updatedBooking = bookingService.update(id,
                targetBooking.getConfirmationCode(), targetBooking.getActive(),
                targetBooking.getLayoverCount(), targetBooking.getTotalPrice());
        BookingResponseDto updatedBookingDto = convertToResponseDto(
                updatedBooking);
        return ResponseEntity.ok(updatedBookingDto);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_AGENT')")
    @DeleteMapping("bookings/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id)
            throws ModelMapperFailedException {
        bookingService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_AGENT')")
    @GetMapping("bookings/email/{confirmationCode}")
    public ResponseEntity<Void> sendBookingEmail(
            @PathVariable String confirmationCode,
            @RequestHeader("Authorization") String bearerToken) {

        Booking booking = bookingService
                .findByConfirmationCode(confirmationCode);

        checkUsernameRequestMatchesResponse(bearerToken,
                booking.getUser().getUsername());

        bookingService.sendEmail(booking);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_AGENT')")
    @PutMapping("bookings/refund")
    public ResponseEntity<BookingResponseDto> refundBooking(
            @RequestParam("id") Long bookingId,
            @RequestParam("refundAmount") Float refundAmount)
            throws StripeException {
        bookingService.refundBooking(bookingId, refundAmount.longValue());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_AGENT')")
    @PostMapping("bookings/purchase")
    public ResponseEntity<BookingResponseDto> purchaseBooking(
            @Valid @RequestBody BookingPurchaseDto bookingPurchaseDto,
            UriComponentsBuilder builder) {

        //Right now does not incorporate discounts
        Discount discount = new Discount();
        discount.setDiscountType("none");
        discount.setDiscountRate(1d);

        List<Passenger> passengers = new ArrayList<>();
        for (PassengerPurchaseDto passengerDto : bookingPurchaseDto.getPassengers()) {
            Passenger passenger = new Passenger();
            passenger.setGivenName(passengerDto.getGivenName());
            passenger.setFamilyName(passengerDto.getFamilyName());
            passenger.setAddress(passengerDto.getAddress());
            passenger.setFlight(flightService.findById(passengerDto.getFlightId()));
            passenger.setDateOfBirth(passengerDto.getDateOfBirth());
            passenger.setCheckInGroup(passengerDto.getCheckInGroup());
            passenger.setSeatNumber(passengerDto.getSeatNumber());
            passenger.setSeatClass(passengerDto.getSeatClass());
            passenger.setGender(passengerDto.getGender());
            passenger.setDiscount(discount);

            passengers.add(passenger);
        }

        Booking createdBooking = bookingService.purchaseBooking(passengers, bookingPurchaseDto.getLayoverCount(),
                                bookingPurchaseDto.getTotalPrice(), bookingPurchaseDto.getUsername(), bookingPurchaseDto.getStripeId());

        BookingResponseDto createdBookingDto = convertToResponseDto(createdBooking);
        createdBookingDto.setRefunded(false);
        createdBookingDto.setStripeId(bookingPurchaseDto.getStripeId());
        return ResponseEntity.created(builder.path("/bookings/{id}").build(createdBookingDto.getId()))
                .body(createdBookingDto);
    }

    private BookingResponseDto convertToResponseDto(Booking booking) {
        return modelMapper.map(booking, BookingResponseDto.class);
    }

    private void checkUsernameRequestMatchesResponse(String bearerToken,
            String responseUsername) throws ResponseStatusException {
        try {
            String jwtToken = bearerToken.replace(JwtUtils.TOKEN_PREFIX, "");
            DecodedJWT jwt = JWT.decode(jwtToken);
            String username = jwt.getSubject();

            Claim claim = jwt.getClaim("authorities");
            List<HashMap> authorities = claim.asList(HashMap.class);
            String role = (String) authorities.get(0).get("authority");

            if (!role.contains("ADMIN") && !username.equals(responseUsername)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Only admins can access another user's information.");
            }
        } catch (JWTDecodeException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "JWT decoding failed.", exception);
        }
    }

    @ExceptionHandler(StripeException.class)
    public String handleError(StripeException e) {
        return e.getMessage();
    }
}
