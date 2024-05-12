package com.example.samuraitravel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitravel.entity.Payment;
import com.example.samuraitravel.repository.PaymentRepository;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    @Transactional
    public void create(String customerId, String subscriptionId, String reason, Integer amount) {
        Payment payment = new Payment();
        payment.setCustomerId(customerId);
        payment.setReason(reason);
        payment.setSubscriptionId(subscriptionId);
        payment.setAmount(amount);;
        paymentRepository.save(payment);
    }
}
