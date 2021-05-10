package com.utopia.bookingservice.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "booking")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "confirmation_code")
    private String confirmationCode;

    @Column(name = "layover_count")
    private Long layoverCount;

    @Column(name = "total_price")
    private Double totalPrice;
}
