package com.utopia.bookingservice.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import com.utopia.bookingservice.dto.PassengerCreationDto;
import com.utopia.bookingservice.dto.PassengerResponseDto;
import com.utopia.bookingservice.dto.PassengerUpdateDto;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.propertymap.PassengerCreationDtoMap;
import com.utopia.bookingservice.propertymap.PassengerGetDtoMap;
import com.utopia.bookingservice.propertymap.PassengerMap;
import com.utopia.bookingservice.service.PassengerService;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("passengers")
public class PassengerController {
    private static final Integer CHILD_DISCOUNT_AGE = 2;
    private static final Integer ELDER_DISCOUNT_AGE = 65;
    private static final Double LAYOVER_DISCOUNT_RATE = 0.1;

    private final PassengerService passengerService;
    private final ModelMapper modelMapper;

    public PassengerController(PassengerService passengerService,
            ModelMapper modelMapper) {
        this.passengerService = passengerService;
        this.modelMapper = modelMapper;
        this.modelMapper.addMappings(new PassengerMap());
        this.modelMapper.addMappings(new PassengerGetDtoMap());
        this.modelMapper.addMappings(new PassengerCreationDtoMap());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<PassengerResponseDto>> findAll(
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize) {
        final Page<Passenger> passengersPage = passengerService
                .findAll(pageIndex, pageSize);
        final Page<PassengerResponseDto> passengerDtosPage = passengersPage
                .map(this::convertToResponseDto);
        return ResponseEntity.ok(passengerDtosPage);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @GetMapping("{id}")
    public ResponseEntity<PassengerResponseDto> findById(
            @PathVariable Long id) {
        final Passenger passenger = passengerService.findById(id);
        final PassengerResponseDto passengerDto = modelMapper.map(passenger,
                PassengerResponseDto.class);
        return ResponseEntity.ok(passengerDto);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @GetMapping("search")
    public ResponseEntity<Page<PassengerResponseDto>> findByConfirmationCodeOrUsernameContaining(
            @RequestParam("term") String searchTerm,
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize) {
        Page<Passenger> passengersPage = passengerService
                .findByConfirmationCodeOrUsernameContaining(searchTerm,
                        pageIndex, pageSize);
        Page<PassengerResponseDto> passengerDtosPage = passengersPage
                .map(this::convertToResponseDto);
        return ResponseEntity.ok(passengerDtosPage);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @GetMapping("distinct_search")
    public ResponseEntity<Page<PassengerResponseDto>> findDistinctByConfirmationCodeOrUsernameContaining(
            @RequestParam("term") String searchTerm,
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize) {
        Page<Passenger> passengers = passengerService
                .findDistinctByConfirmationCodeOrUsernameContaining(searchTerm,
                        pageIndex, pageSize);
        Page<PassengerResponseDto> passengerDtos = passengers
                .map(this::convertToResponseDto);
        return ResponseEntity.ok(passengerDtos);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_AGENT')")
    @PostMapping
    public ResponseEntity<PassengerResponseDto> create(
            @Valid @RequestBody PassengerCreationDto passengerCreationDto,
            UriComponentsBuilder builder) {
        Passenger passengerToCreate = modelMapper.map(passengerCreationDto,
                Passenger.class);

        String originAirportCode = passengerCreationDto.getOriginAirportCode();
        String destinationAirportCode = passengerCreationDto
                .getDestinationAirportCode();
        String airplaneModel = passengerCreationDto.getAirplaneModel();
        LocalDateTime departureTime = passengerCreationDto.getDepartureTime();
        LocalDateTime arrivalTime = passengerCreationDto.getArrivalTime();
        String seatClass = passengerCreationDto.getSeatClass();
        LocalDate dateOfBirth = passengerCreationDto.getDateOfBirth();

        Passenger createdPassenger = passengerService.create(passengerToCreate,
                originAirportCode, destinationAirportCode, airplaneModel,
                departureTime, arrivalTime, seatClass, dateOfBirth);
        PassengerResponseDto createdPassengerDto = modelMapper
                .map(createdPassenger, PassengerResponseDto.class);
        Long id = createdPassengerDto.getId();
        return ResponseEntity
                .created(builder.path("/passengers/{id}").build(id))
                .body(createdPassengerDto);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_AGENT')")
    @PutMapping("{id}")
    public ResponseEntity<PassengerResponseDto> update(@PathVariable Long id,
            @Valid @RequestBody PassengerUpdateDto passengerUpdateDto,
            UriComponentsBuilder builder) {
        Passenger updatedPassenger = passengerService.update(id,
                passengerUpdateDto.getGivenName(),
                passengerUpdateDto.getFamilyName(),
                passengerUpdateDto.getDateOfBirth(),
                passengerUpdateDto.getGender(), passengerUpdateDto.getAddress(),
                passengerUpdateDto.getSeatClass(),
                passengerUpdateDto.getSeatNumber(),
                passengerUpdateDto.getCheckInGroup());
        PassengerResponseDto updatedPassengerDto = convertToResponseDto(
                updatedPassenger);
        return ResponseEntity.ok(updatedPassengerDto);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_AGENT')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        passengerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/get-by-flight-id")
    public ResponseEntity<List<Passenger>> findByFlightId(@PathVariable Long flightId) {
        List<Passenger> passengers = passengerService.getPassengersByFlightId(flightId);
        return ResponseEntity.ok(passengers);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER')")
    @GetMapping("/taken-seats")
    public List<Integer> getTakenSeats(@RequestParam Long flightId) {
        System.out.println(flightId);
        System.out.println(passengerService.getTakenSeats(flightId));
        return passengerService.getTakenSeats(flightId);
    }

    private PassengerResponseDto convertToResponseDto(Passenger passenger) {
        return modelMapper.map(passenger, PassengerResponseDto.class);
    }
}
