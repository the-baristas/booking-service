package com.utopia.bookingservice.controller;

import static org.mockito.Mockito.when;

import java.util.Arrays;

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

    @MockBean
    private BookingService bookingService;

    @Value("${jwt.secret-key")
    private String jwtSecretKey;

    @BeforeEach
    public void setUp() {
        webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build();
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    public void findAll_BookingsFound() throws JsonProcessingException {
        Booking booking = new Booking();
        Long id = 1L;
        booking.setId(id);
        booking.setActive(Boolean.TRUE);
        booking.setConfirmationCode("confirmation_code");
        booking.setLayoverCount(0);
        booking.setTotalPrice(10.01);
        Page<Booking> foundBookingsPage = new PageImpl<Booking>(
                Arrays.asList(booking));
        Integer pageIndex = 0;
        Integer pageSize = 1;
        when(bookingService.findAll(pageIndex, pageSize))
                .thenReturn(foundBookingsPage);
        Page<BookingResponseDto> foundBookingDtosPage = foundBookingsPage
                .map((Booking b) -> modelMapper.map(b, BookingResponseDto.class));

        webTestClient.get()
                .uri("/bookings?index={pageIndex}&size={pageSize}", pageIndex,
                        pageSize)
                .exchange().expectStatus().isOk().expectBody(String.class)
                .isEqualTo(new ObjectMapper()
                        .writeValueAsString(foundBookingDtosPage));
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    public void findByConfirmationCodeContaining_ValidConfirmationCode_BookingsFound() {
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    public void findByUsername_ValidUsername_BookingsFound() {
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
