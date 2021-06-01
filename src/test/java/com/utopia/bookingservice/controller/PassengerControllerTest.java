package com.utopia.bookingservice.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utopia.bookingservice.dto.PassengerDto;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.service.PassengerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

@Disabled("Temporary for Jenkins.")
@WebMvcTest(PassengerController.class)
public class PassengerControllerTest {
    private WebTestClient webTestClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelMapper modelMapper;

    @MockBean
    private PassengerService passengerService;

    @BeforeEach
    public void setUp() {
        webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build();
    }

    @Test
    public void findAllPassengers_PassengersFound()
            throws JsonProcessingException, Exception {
        Passenger passenger = new Passenger();
        Page<Passenger> foundPassengers = new PageImpl<Passenger>(
                Arrays.asList(passenger));
        when(passengerService.findAll(0, 1)).thenReturn(foundPassengers);
        Page<PassengerDto> foundPassengerDtosPage = foundPassengers
                .map((Passenger p) -> modelMapper.map(p, PassengerDto.class));

        webTestClient.get().uri("/passengers?index=0&size=1")
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
                .isOk().expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class).isEqualTo(new ObjectMapper()
                        .writeValueAsString(foundPassengerDtosPage));

        mockMvc.perform(get("/passengers?index=0&size=1")
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(new ObjectMapper()
                        .writeValueAsString(foundPassengerDtosPage)));
    }

    @Test
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
        Page<PassengerDto> foundPassengerDtosPage = foundPassengersPage
                .map((Passenger p) -> modelMapper.map(p, PassengerDto.class));

        mockMvc.perform(
                get("/passengers/search?term={searchTerm}&index=0&size=1", searchTerm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(new ObjectMapper()
                        .writeValueAsString(foundPassengerDtosPage)));
    }

    @Test
    public void findDistinctByConfirmationCodeOrUsernameContaining_ValidSearchTerm_PassengersFound()
            throws JsonProcessingException, Exception {
        Passenger passenger = new Passenger();
        Page<Passenger> foundPassengersPage = new PageImpl<Passenger>(
                Arrays.asList(passenger));
        String searchTerm = "a";
        Integer pageIndex = 0;
        Integer pageSize = 1;
        when(passengerService.findDistinctByConfirmationCodeOrUsernameContaining(
                searchTerm, pageIndex, pageSize))
                        .thenReturn(foundPassengersPage);
        Page<PassengerDto> foundPassengerDtosPage = foundPassengersPage
                .map((Passenger p) -> modelMapper.map(p, PassengerDto.class));

        mockMvc.perform(
                get("/passengers/distinct_search?term={searchTerm}&index=0&size=1", searchTerm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(new ObjectMapper()
                        .writeValueAsString(foundPassengerDtosPage)));
    }

    @Test
    public void createPassenger_ValidPassenger_PassengerCreated() {
        Passenger passenger = new Passenger();
        when(passengerService.create(passenger)).thenReturn(passenger);
        PassengerDto passengerDto = modelMapper.map(passenger,
                PassengerDto.class);

        webTestClient.post().uri("/passengers")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(passengerDto)
                .exchange().expectStatus().isCreated().expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(PassengerDto.class).isEqualTo(passengerDto);
    }

    @Test
    public void updatePassenger_ValidPassenger_PassengerUpdated() {
        Passenger passenger = new Passenger();
        Long id = 1L;
        passenger.setId(id);
        when(passengerService.update(passenger)).thenReturn(passenger);
        PassengerDto passengerDto = modelMapper.map(passenger,
                PassengerDto.class);

        webTestClient.put().uri("/passengers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON).bodyValue(passengerDto)
                .exchange().expectStatus().isOk().expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(PassengerDto.class).isEqualTo(passengerDto);
    }

    @Test
    public void deletePassengerById_ValidId_PassengerDeleted() {
        Passenger passenger = new Passenger();
        Long id = 1L;
        passenger.setId(id);

        passengerService.deleteById(id);
        verify(passengerService, times(1)).deleteById(id);
    }
}
