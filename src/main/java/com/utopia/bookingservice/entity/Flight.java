package com.utopia.bookingservice.entity;

import java.time.LocalDateTime;
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
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@Data
@Entity
@Table(name = "flight")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne
    @JoinColumn(name = "airplane_id")
    private Airplane airplane;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "departure_gate")
    private String departureGate;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "arrival_gate")
    private String arrivalGate;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "first_reserved")
    private Integer reservedFirstClassSeatsCount;

    @Column(name = "first_price")
    private Double firstClassPrice;

    @Column(name = "business_reserved")
    private Integer reservedBusinessClassSeatsCount;

    @Column(name = "business_price")
    private Double businessClassPrice;

    @Column(name = "economy_reserved")
    private Integer reservedEconomyClassSeatsCount;

    @Column(name = "economy_price")
    private Double economyClassPrice;

    @OneToMany(mappedBy = "flight")
    private List<Passenger> passengers;

    @ManyToMany
    @JoinTable(name = "flight_user",
            joinColumns = @JoinColumn(name = "flight_id",
                    referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id",
                    referencedColumnName = "id"))
    private Set<User> users;
}
