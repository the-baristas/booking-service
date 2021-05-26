package com.utopia.bookingservice.controller;

import java.text.ParseException;

import javax.validation.Valid;

import com.utopia.bookingservice.dto.PassengerDto;
import com.utopia.bookingservice.entity.Passenger;
import com.utopia.bookingservice.exception.ModelMapperFailedException;
import com.utopia.bookingservice.propertymap.PassengerMap;
import com.utopia.bookingservice.service.PassengerService;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
    private final PassengerService passengerService;
    private final ModelMapper modelMapper;

    public PassengerController(PassengerService passengerService,
            ModelMapper modelMapper) {
        this.passengerService = passengerService;
        this.modelMapper = modelMapper;
        this.modelMapper.addMappings(new PassengerMap());
        this.modelMapper
                .addMappings(new PropertyMap<PassengerDto, Passenger>() {
                    @Override
                    protected void configure() {
                        map().getBooking().setId(source.getBookingId());
                        map().getBooking().setActive(source.getBookingActive());
                        map().getBooking().setConfirmationCode(
                                source.getBookingConfirmationCode());
                        map().getBooking()
                                .setLayoverCount(source.getLayoverCount());
                        map().getBooking()
                                .setTotalPrice(source.getBookingTotalPrice());

                        map().getBooking().getUser()
                                .setUsername(source.getUsername());

                        map().getFlight().setId(source.getFlightId());
                        map().getFlight().setActive(source.getFlightActive());
                        map().getFlight()
                                .setDepartureTime(source.getDepartureTime());
                        map().getFlight()
                                .setArrivalTime(source.getArrivalTime());

                        map().getFlight().getRoute().setId(source.getRouteId());
                        map().getFlight().getRoute()
                                .setActive(source.getRouteActive());

                        map().getFlight().getRoute().getOriginAirport()
                                .setIataId(source.getOriginAirportCode());
                        map().getFlight().getRoute().getOriginAirport()
                                .setActive(source.getOriginAirportActive());
                        map().getFlight().getRoute().getOriginAirport()
                                .setCity(source.getOriginAirportCity());

                        map().getFlight().getRoute().getDestinationAirport()
                                .setIataId(source.getDestinationAirportCode());
                        map().getFlight().getRoute().getDestinationAirport()
                                .setActive(
                                        source.getDestinationAirportActive());
                        map().getFlight().getRoute().getDestinationAirport()
                                .setCity(source.getDestinationAirportCity());

                        map().getDiscount()
                                .setDiscountType(source.getDiscountType());
                        map().getDiscount()
                                .setDiscountRate(source.getDiscountRate());
                    }
                });
    }

    @GetMapping
    public ResponseEntity<Page<PassengerDto>> findAll(
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize) {
        final Page<Passenger> passengers = passengerService.findAll(pageIndex,
                pageSize);
        final Page<PassengerDto> passengerDtos = passengers
                .map(this::convertPassengerToDto);
        return ResponseEntity.ok(passengerDtos);
    }

    @GetMapping("{id}")
    public ResponseEntity<PassengerDto> findById(@PathVariable Long id) {
        final Passenger passenger = passengerService.findById(id);
        final PassengerDto passengerDto = this.convertPassengerToDto(passenger);
        return ResponseEntity.ok(passengerDto);
    }

    @GetMapping("search")
    public ResponseEntity<Page<PassengerDto>> findByConfirmationCodeOrUsernameContaining(
            @RequestParam("term") String searchTerm,
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize) {
        Page<Passenger> passengers = passengerService
                .findByConfirmationCodeOrUsernameContaining(searchTerm,
                        pageIndex, pageSize);
        Page<PassengerDto> passengerDtos = passengers
                .map(this::convertPassengerToDto);
        return ResponseEntity.ok(passengerDtos);
    }

    @GetMapping("distinct_search")
    public ResponseEntity<Page<PassengerDto>> findDistinctByConfirmationCodeOrUsernameContaining(
            @RequestParam("term") String searchTerm,
            @RequestParam("index") Integer pageIndex,
            @RequestParam("size") Integer pageSize) {
        Page<Passenger> passengers = passengerService
                .findDistinctByConfirmationCodeOrUsernameContaining(searchTerm,
                        pageIndex, pageSize);
        Page<PassengerDto> passengerDtos = passengers
                .map(this::convertPassengerToDto);
        return ResponseEntity.ok(passengerDtos);
    }

    @PostMapping
    public ResponseEntity<PassengerDto> create(
            @Valid @RequestBody PassengerDto passengerDto,
            UriComponentsBuilder builder) {
        Passenger passenger;
        try {
            passenger = convertDtoToPassenger(passengerDto);
        } catch (ParseException e) {
            throw new ModelMapperFailedException(e);
        }
        Passenger createdPassenger = passengerService.create(passenger);
        return ResponseEntity
                .created(builder.path("/passengers/{id}")
                        .build(passengerDto.getId()))
                .body(convertPassengerToDto(createdPassenger));
    }

    @PutMapping("{id}")
    public ResponseEntity<PassengerDto> update(@PathVariable Long id,
            @Valid @RequestBody PassengerDto passengerDto,
            UriComponentsBuilder builder) {
        passengerDto.setId(id);
        Passenger passenger;
        try {
            passenger = convertDtoToPassenger(passengerDto);
        } catch (ParseException e) {
            throw new ModelMapperFailedException(e);
        }
        Passenger updatedPassenger = passengerService.update(passenger);
        return ResponseEntity.ok(convertPassengerToDto(updatedPassenger));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        passengerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private PassengerDto convertPassengerToDto(Passenger passenger) {
        return modelMapper.map(passenger, PassengerDto.class);
    }

    private Passenger convertDtoToPassenger(PassengerDto passengerDto)
            throws ParseException {
        return modelMapper.map(passengerDto, Passenger.class);
    }
}
