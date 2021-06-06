package com.utopia.bookingservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "stripe_id")
    private String stripeId;

    @Column(name = "refunded")
    private boolean refunded;
}
