package com.utopia.bookingservice.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utopia.bookingservice.dto.BookingCreationDto;
import com.utopia.bookingservice.dto.BookingResponseDto;
import com.utopia.bookingservice.dto.BookingUpdateDto;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BookingControllerTest {
    private WebTestClient webTestClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired

    @MockBean
    private BookingService bookingService;

    @Value("${jwt.secret-key")
    private String jwtSecretKey;

    private ObjectMapper objectMapper = new ObjectMapper();

    private String jwtToken;

    @BeforeEach
    public void setUp() throws IllegalArgumentException, JWTCreationException,
            JsonProcessingException {
        webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build();

        Map<String, String> authorityMap = new HashMap<>();
        authorityMap.put("authority", "ROLE_ADMIN");
        jwtToken = JWT.create().withSubject("username")
                .withClaim("authorities", Arrays.asList(authorityMap))
                .withExpiresAt(new Date(System.currentTimeMillis() + 900_000))
                .sign(Algorithm.HMAC512(jwtSecretKey.getBytes()));
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
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
    @WithMockUser(authorities = { "ROLE_ADMIN" })
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
    @WithMockUser(authorities = { "ROLE_ADMIN" })
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
    @WithMockUser(authorities = { "ROLE_ADMIN" })
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

        webTestClient.get().uri(
                "/bookings/username/{username}?index={pageIndex}&size={pageSize}",
                username, pageIndex, pageSize).header("Authorization", jwtToken)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .isEqualTo(
                        objectMapper.writeValueAsString(foundBookingDtosPage));
    }

    @Test
    @WithMockUser(authorities = { "ROLE_CUSTOMER" })
    public void create_ValidBookingCreationDto_BookingCreated()
            throws JsonProcessingException {
        BookingCreationDto creationDto = new BookingCreationDto();
        creationDto.setConfirmationCode("a");
        creationDto.setLayoverCount(0);
        String username = "username";
        creationDto.setUsername(username);

        Booking bookingToCreate = modelMapper.map(creationDto, Booking.class);
        Booking createdBooking = objectMapper.readValue(
                objectMapper.writeValueAsString(bookingToCreate),
                Booking.class);
        when(bookingService.create(username, bookingToCreate))
                .thenReturn(createdBooking);

        BookingResponseDto responseDto = modelMapper.map(createdBooking,
                BookingResponseDto.class);

        webTestClient.post().uri("/bookings").header("Authorization", jwtToken)
                .bodyValue(creationDto).exchange().expectStatus().isCreated()
                .expectBody(BookingResponseDto.class).isEqualTo(responseDto);
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void update_ValidBookingUpdateDto_BookingUpdated()
            throws JsonMappingException, JsonProcessingException {
        BookingUpdateDto updateDto = new BookingUpdateDto();
        String confirmationCode = "confirmation_code";
        updateDto.setConfirmationCode(confirmationCode);
        Integer layoverCount = 0;
        Boolean active = Boolean.TRUE;
        updateDto.setActive(active);
        updateDto.setLayoverCount(layoverCount);
        Double totalPrice = 1.01;
        updateDto.setTotalPrice(totalPrice);

        Long id = 1L;
        Booking targetBooking = modelMapper.map(updateDto, Booking.class);
        Booking updatedBooking = objectMapper.readValue(
                objectMapper.writeValueAsString(targetBooking), Booking.class);
        when(bookingService.update(id, confirmationCode, active, layoverCount,
                totalPrice)).thenReturn(updatedBooking);

        BookingResponseDto responseDto = modelMapper.map(updatedBooking,
                BookingResponseDto.class);

        webTestClient.put().uri("/bookings/{id}", id).bodyValue(updateDto)
                .exchange().expectStatus().isOk()
                .expectBody(BookingResponseDto.class).isEqualTo(responseDto);
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void deleteById_ValidId_NoContent() {
        Long id = 1L;

        webTestClient.delete().uri("/bookings/{id}", id).exchange()
                .expectStatus().isNoContent();
        verify(bookingService, times(1)).deleteById(id);
    }
}
