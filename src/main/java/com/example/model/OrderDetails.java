package com.example.model;

import com.example.model.abstracts.BaseEntity;

import javax.persistence.*;

@Entity
@Table(name = "order_details")
public class OrderDetails extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "total")
    private double totalPrice;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private PaymentDetails paymentDetails;

}
