package com.utopia.bookingservice.controller;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utopia.bookingservice.dto.BookingResponseDto;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.service.BookingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BookingControllerTest {
    private WebTestClient webTestClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelMapper modelMapper;

    @MockBean
    private BookingService bookingService;

    @Value("${jwt.secret-key")
    private String jwtSecretKey;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build();
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    public void findAll_BookingsFound() throws JsonProcessingException {
        Booking foundBooking = new Booking();
        Long id = 1L;
        foundBooking.setId(id);
        foundBooking.setActive(Boolean.TRUE);
        foundBooking.setConfirmationCode("confirmation_code");
        foundBooking.setLayoverCount(0);
        foundBooking.setTotalPrice(10.01);
        Page<Booking> foundBookingsPage = new PageImpl<Booking>(
                Arrays.asList(foundBooking));
        Integer pageIndex = 0;
        Integer pageSize = 1;
        when(bookingService.findAll(pageIndex, pageSize))
                .thenReturn(foundBookingsPage);
        Page<BookingResponseDto> foundBookingDtosPage = foundBookingsPage.map(
                (Booking b) -> modelMapper.map(b, BookingResponseDto.class));

        webTestClient.get()
                .uri("/bookings?index={pageIndex}&size={pageSize}", pageIndex,
                        pageSize)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .isEqualTo(
                        objectMapper.writeValueAsString(foundBookingDtosPage));
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    public void findByConfirmationCode_ValidConfirmationCode_BookingsFound()
            throws JsonProcessingException {
        Booking foundBooking = new Booking();
        Long id = 1L;
        foundBooking.setId(id);
        foundBooking.setActive(Boolean.TRUE);
        String confirmationCode = "confirmation_code";
        foundBooking.setConfirmationCode(confirmationCode);
        foundBooking.setLayoverCount(0);
        foundBooking.setTotalPrice(10.01);
        when(bookingService.findByConfirmationCode(confirmationCode))
                .thenReturn(foundBooking);
        BookingResponseDto foundBookingDto = modelMapper.map(foundBooking,
                BookingResponseDto.class);

        webTestClient.get()
                .uri("/bookings/{confirmation_code}", confirmationCode)
                .exchange().expectStatus().isOk()
                .expectBody(BookingResponseDto.class)
                .isEqualTo(foundBookingDto);
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    public void findByConfirmationCodeContaining_ValidConfirmationCode_BookingsFound()
            throws JsonProcessingException {
        Booking foundBooking = new Booking();
        Long id = 1L;
        foundBooking.setId(id);
        foundBooking.setActive(Boolean.TRUE);
        String confirmationCode = "confirmation_code";
        foundBooking.setConfirmationCode(confirmationCode);
        foundBooking.setLayoverCount(0);
        foundBooking.setTotalPrice(10.01);
        String searchTerm = "con";
        Integer pageIndex = 0;
        Integer pageSize = 1;
        Page<Booking> foundBookingsPage = new PageImpl<Booking>(
                Arrays.asList(foundBooking));
        when(bookingService.findByConfirmationCodeContaining(searchTerm,
                pageIndex, pageSize)).thenReturn(foundBookingsPage);

        Page<BookingResponseDto> foundBookingDtosPage = foundBookingsPage.map(
                (Booking b) -> modelMapper.map(b, BookingResponseDto.class));
        webTestClient.get().uri(
                "/bookings/search?confirmation_code={searchTerm}&index={pageIndex}&size={pageSize}",
                searchTerm, pageIndex, pageSize).exchange().expectStatus()
                .isOk().expectBody(String.class).isEqualTo(
                        objectMapper.writeValueAsString(foundBookingDtosPage));
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    public void findByUsername_ValidUsername_BookingsFound()
            throws JsonProcessingException {
        Booking foundBooking = new Booking();
        Long id = 1L;
        foundBooking.setId(id);
        foundBooking.setActive(Boolean.TRUE);
        String confirmationCode = "confirmation_code";
        foundBooking.setConfirmationCode(confirmationCode);
        foundBooking.setLayoverCount(0);
        foundBooking.setTotalPrice(10.01);
        String username = "username";
        Integer pageIndex = 0;
        Integer pageSize = 1;
        Page<Booking> foundBookingsPage = new PageImpl<Booking>(
                Arrays.asList(foundBooking));
        when(bookingService.findByUsername(username, pageIndex, pageSize))
                .thenReturn(foundBookingsPage);

        Page<BookingResponseDto> foundBookingDtosPage = foundBookingsPage.map(
                (Booking b) -> modelMapper.map(b, BookingResponseDto.class));
        Map<String, String> authorityMap = new HashMap<>();
        authorityMap.put("authority", "ROLE_ADMIN");
        String jwtToken = JWT.create().withSubject("username")
                .withClaim("authorities",
                        Arrays.asList(authorityMap.toString()))
                .withExpiresAt(new Date(System.currentTimeMillis() + 900_000))
                .sign(Algorithm.HMAC512(jwtSecretKey.getBytes()));

        // TODO: Move to controller.
        try {
            DecodedJWT jwt = JWT.decode(jwtToken);
            Claim claim = jwt.getClaim("authorities");
            System.out.printf("Claim is null: %s%n", claim.isNull());
            List<String> list = claim.asList(String.class);
            String rolesMapString = list.get(0);
            Map<String, String> map = objectMapper.readValue(rolesMapString,
                    Map.class);
            System.out.printf("rolesMapString: %s%n", rolesMapString);
        } catch (JWTDecodeException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        webTestClient.get().uri(
                "/bookings/username/{username}?index={pageIndex}&size={pageSize}",
                username, pageIndex, pageSize).header("Authorization", jwtToken)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .isEqualTo(
                        objectMapper.writeValueAsString(foundBookingDtosPage));
    }

    @Test
    @WithMockUser(roles = { "CUSTOMER" })
    public void create_ValidBookingCreationDto_BookingCreated() {
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    public void update_ValidBookingUpdateDto_BookingUpdated() {
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    public void delete_ValidId_BookingDeleted() {
    }
}
