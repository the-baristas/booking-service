package com.utopia.bookingservice.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utopia.bookingservice.dto.PassengerCreationDto;
import com.utopia.bookingservice.dto.PassengerResponseDto;
import com.utopia.bookingservice.dto.PassengerUpdateDto;
import com.utopia.bookingservice.entity.Airport;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Discount;
import com.utopia.bookingservice.entity.Flight;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.entity.Route;
import com.utopia.bookingservice.entity.User;
import com.utopia.bookingservice.service.PassengerService;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

@WebMvcTest(PassengerController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PassengerControllerTest {
    private WebTestClient webTestClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelMapper modelMapper;

    @MockBean
    private PassengerService passengerService;

    @Value("${jwt.secret-key")
    private String jwtSecretKey;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build();
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void findAll_PassengersFound()
            throws JsonProcessingException, Exception {
        Passenger passenger = new Passenger();
        Page<Passenger> foundPassengersPage = new PageImpl<Passenger>(
                Arrays.asList(passenger));
        when(passengerService.findAll(0, 1)).thenReturn(foundPassengersPage);
        Page<PassengerResponseDto> foundPassengerDtosPage = foundPassengersPage
                .map((Passenger p) -> modelMapper.map(p,
                        PassengerResponseDto.class));

        webTestClient.get().uri("/passengers?index=0&size=1")
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
                .isOk().expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class).isEqualTo(objectMapper
                        .writeValueAsString(foundPassengerDtosPage));

        mockMvc.perform(get("/passengers?index=0&size=1")
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper
                        .writeValueAsString(foundPassengerDtosPage)));
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void findByConfirmationCodeOrUsernameContaining_ValidSearchTerm_PassengersFound()
            throws JsonProcessingException, Exception {
        Passenger passenger = new Passenger();
        Page<Passenger> foundPassengersPage = new PageImpl<Passenger>(
                Arrays.asList(passenger));
        String searchTerm = "a";
        Integer pageIndex = 0;
        Integer pageSize = 1;
        when(passengerService.findByConfirmationCodeOrUsernameContaining(
                searchTerm, pageIndex, pageSize))
                        .thenReturn(foundPassengersPage);
        Page<PassengerResponseDto> foundPassengerDtosPage = foundPassengersPage
                .map((Passenger p) -> modelMapper.map(p,
                        PassengerResponseDto.class));

        mockMvc.perform(
                get("/passengers/search?term={searchTerm}&index=0&size=1",
                        searchTerm).contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper
                        .writeValueAsString(foundPassengerDtosPage)));
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void findDistinctByConfirmationCodeOrUsernameContaining_ValidSearchTerm_PassengersFound()
            throws JsonProcessingException, Exception {
        Passenger passenger = new Passenger();
        Page<Passenger> foundPassengersPage = new PageImpl<Passenger>(
                Arrays.asList(passenger));
        String searchTerm = "a";
        Integer pageIndex = 0;
        Integer pageSize = 1;
        when(passengerService
                .findDistinctByConfirmationCodeOrUsernameContaining(searchTerm,
                        pageIndex, pageSize)).thenReturn(foundPassengersPage);
        Page<PassengerResponseDto> foundPassengerDtosPage = foundPassengersPage
                .map((Passenger p) -> modelMapper.map(p,
                        PassengerResponseDto.class));

        mockMvc.perform(get(
                "/passengers/distinct_search?term={searchTerm}&index=0&size=1",
                searchTerm).contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper
                        .writeValueAsString(foundPassengerDtosPage)));
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void createPassenger_ValidPassenger_PassengerCreated() {
        PassengerCreationDto passengerCreationDto = new PassengerCreationDto();
        String bookingConfirmationCode = "confirmation_code";
        String originAirportCode = "ABC";
        String destinationAirportCode = "BCD";
        String airplaneModel = "model";
        LocalDateTime departureTime = LocalDateTime.of(2022, 1, 1, 1, 0);
        LocalDateTime arrivalTime = LocalDateTime.of(2022, 1, 1, 2, 0);
        passengerCreationDto
                .setBookingConfirmationCode(bookingConfirmationCode);
        passengerCreationDto.setOriginAirportCode(originAirportCode);
        passengerCreationDto.setDestinationAirportCode(destinationAirportCode);
        passengerCreationDto.setAirplaneModel(airplaneModel);
        passengerCreationDto.setDepartureTime(departureTime);
        passengerCreationDto.setArrivalTime(arrivalTime);
        String givenName = "given_name";
        passengerCreationDto.setGivenName(givenName);
        String familyName = "family_name";
        passengerCreationDto.setFamilyName(familyName);
        LocalDate dateOfBirth = LocalDate.of(2000, 1, 1);
        passengerCreationDto.setDateOfBirth(dateOfBirth);
        String gender = "gender";
        passengerCreationDto.setGender(gender);
        String address = "1 Main Street Test City, FL 12345";
        passengerCreationDto.setAddress(address);
        String seatClass = "first";
        passengerCreationDto.setSeatClass(seatClass);
        Integer seatNumber = 1;
        passengerCreationDto.setSeatNumber(seatNumber);
        Integer checkInGroup = 1;
        passengerCreationDto.setCheckInGroup(checkInGroup);

        Passenger passengerToCreate = modelMapper.map(passengerCreationDto,
                Passenger.class);
        Passenger createdPassenger = new Passenger();
        PassengerResponseDto createdPassengerDto = modelMapper
                .map(createdPassenger, PassengerResponseDto.class);
        when(passengerService.create(passengerToCreate, originAirportCode,
                destinationAirportCode, airplaneModel, departureTime,
                arrivalTime, seatClass, dateOfBirth))
                        .thenReturn(createdPassenger);

        String jwtToken = JWT.create().withSubject("username")
                .withExpiresAt(new Date(System.currentTimeMillis() + 900_000))
                .sign(Algorithm.HMAC512(jwtSecretKey.getBytes()));

        webTestClient.post().uri("/passengers")
                .headers((HttpHeaders headers) -> {
                    headers.add("Authorization", jwtToken);
                }).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(passengerCreationDto).exchange().expectStatus()
                .isCreated().expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(PassengerResponseDto.class)
                .isEqualTo(createdPassengerDto);
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void updatePassenger_ValidPassenger_PassengerUpdated()
            throws JsonMappingException, JsonProcessingException {
        PassengerUpdateDto passengerUpdateDto = new PassengerUpdateDto();
        String givenName = "first_name";
        passengerUpdateDto.setGivenName(givenName);
        String familyName = "last_name";
        passengerUpdateDto.setFamilyName(familyName);
        LocalDate dateOfBirth = LocalDate.of(2000, 1, 1);
        passengerUpdateDto.setDateOfBirth(dateOfBirth);
        String gender = "nonbinary";
        passengerUpdateDto.setGender(gender);
        String address = "1 Main Street Test City, FL 12345";
        passengerUpdateDto.setAddress(address);
        String newSeatClass = "first";
        passengerUpdateDto.setSeatClass(newSeatClass);
        Integer seatNumber = 1;
        passengerUpdateDto.setSeatNumber(seatNumber);
        Integer checkInGroup = 1;
        passengerUpdateDto.setCheckInGroup(checkInGroup);

        Passenger updatedPassenger = modelMapper.map(passengerUpdateDto,
                Passenger.class);
        Long passengerId = 1L;
        updatedPassenger.setId(passengerId);
        updatedPassenger.setBooking(new Booking());
        updatedPassenger.getBooking().setId(1L);
        updatedPassenger.getBooking().setActive(Boolean.TRUE);
        updatedPassenger.getBooking().setConfirmationCode("a");
        updatedPassenger.getBooking().setLayoverCount(0);
        updatedPassenger.getBooking().setTotalPrice(0d);
        updatedPassenger.setFlight(new Flight());
        updatedPassenger.getFlight().setId(1L);
        updatedPassenger.getFlight().setActive(Boolean.TRUE);
        updatedPassenger.getFlight()
                .setDepartureTime(LocalDateTime.of(2025, 1, 1, 1, 0, 0));
        updatedPassenger.getFlight()
                .setArrivalTime(LocalDateTime.of(2025, 1, 1, 2, 0, 0));
        Route route = new Route();
        route.setId(1L);
        route.setActive(Boolean.TRUE);
        route.setOriginAirport(new Airport());
        route.getOriginAirport().setAirportCode("ABC");
        route.getOriginAirport().setActive(Boolean.TRUE);
        route.getOriginAirport().setCity("Origin City");
        route.setDestinationAirport(new Airport());
        route.getDestinationAirport().setAirportCode("ABC");
        route.getDestinationAirport().setActive(Boolean.TRUE);
        route.getDestinationAirport().setCity("Destination City");
        updatedPassenger.getFlight().setRoute(route);
        updatedPassenger.setDiscount(new Discount());
        updatedPassenger.getDiscount().setDiscountType("none");
        updatedPassenger.getDiscount().setDiscountRate(1d);
        updatedPassenger.getBooking().setUser(new User());
        updatedPassenger.getBooking().getUser().setUsername("username");
        updatedPassenger.getBooking().getUser().setEmail("email@email.com");
        updatedPassenger.getBooking().getUser().setPhone("5556667777");
        when(passengerService.update(passengerId, givenName, familyName,
                dateOfBirth, gender, address, newSeatClass, seatNumber,
                checkInGroup)).thenReturn(updatedPassenger);

        PassengerResponseDto updatedPassengerDto = modelMapper
                .map(updatedPassenger, PassengerResponseDto.class);

        webTestClient.put().uri("/passengers/{id}", passengerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(passengerUpdateDto).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PassengerResponseDto.class)
                .isEqualTo(updatedPassengerDto);
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void deletePassengerById_ValidId_NoContent() {
        Long id = 1L;

        webTestClient.delete().uri("/passengers/{id}", id).exchange()
                .expectStatus().isNoContent();
        verify(passengerService, times(1)).deleteById(id);
    }
}
