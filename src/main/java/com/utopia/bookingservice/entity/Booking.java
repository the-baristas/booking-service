package com.utopia.bookingservice.entity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@Data
@Entity
@Table(name = "booking")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "confirmation_code", unique = true)
    private String confirmationCode;

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "layover_count")
    private Integer layoverCount;

    @Column(name = "total_price")
    private Double totalPrice;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(name = "passenger",
            joinColumns = @JoinColumn(name = "booking_id",
                    referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "flight_id",
                    referencedColumnName = "id"))
    private Set<Flight> flights;

    @OneToMany(mappedBy = "booking")
    private List<Passenger> passengers;

    @OneToOne(mappedBy = "booking")
    private Payment payment;

    public Flight findEarliestDepartingFlight(){
        return flights.stream().min(Comparator.comparing(Flight::getDepartureTime)).get();
    }
}
