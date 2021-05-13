package com.utopia.bookingservice.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utopia.bookingservice.dto.PassengerDto;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.service.PassengerService;

import org.junit.jupiter.api.BeforeEach;
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
    public void findAllPassengers_PassengersFound() throws Exception {
        Passenger passenger = new Passenger();
        Page<Passenger> foundPassengers = new PageImpl<Passenger>(
                Arrays.asList(passenger));
        when(passengerService.findAllPassengers(0, 1))
                .thenReturn(foundPassengers);
        Page<PassengerDto> foundPassengerDtos = foundPassengers
                .map((Passenger p) -> modelMapper.map(p, PassengerDto.class));

        webTestClient.get().uri("/passengers?index=0&size=1")
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
                .isOk().expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class).isEqualTo(new ObjectMapper()
                        .writeValueAsString(foundPassengerDtos));

        mockMvc.perform(get("/passengers?index=0&size=1")
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(new ObjectMapper()
                        .writeValueAsString(foundPassengerDtos)));
    }

    @Test
    public void createPassenger_ValidPassenger_PassengerCreated() {
        Passenger passenger = new Passenger();
        when(passengerService.createPassenger(passenger)).thenReturn(passenger);
        PassengerDto passengerDto = modelMapper.map(passenger,
                PassengerDto.class);

        webTestClient.post().uri("/passengers")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(passengerDto)
                .exchange().expectStatus().isCreated().expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(PassengerDto.class).isEqualTo(passengerDto);
    }
}
