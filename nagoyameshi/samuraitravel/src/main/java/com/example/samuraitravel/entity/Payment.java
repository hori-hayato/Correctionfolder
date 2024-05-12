package com.example.samuraitravel.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "subscription_id")
    private String subscriptionId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "paid_at", insertable = false, updatable = false)
    private Timestamp paidAt;
}
