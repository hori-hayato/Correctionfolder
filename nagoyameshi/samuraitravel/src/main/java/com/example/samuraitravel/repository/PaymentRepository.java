package com.example.samuraitravel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitravel.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}
