package com.utopia.bookingservice.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "airplane")
public class Airplane {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "model")
    private String model;

    @Column(name = "max_first")
    private Integer maxFirstClassSeatsCount;

    @Column(name = "max_business")
    private Integer maxBusinessClassSeatsCount;

    @Column(name = "max_economy")
    private Integer maxEconomyClassSeatsCount;
}
