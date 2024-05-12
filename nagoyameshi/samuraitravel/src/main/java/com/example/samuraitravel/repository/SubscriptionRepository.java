package com.example.samuraitravel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitravel.entity.Subscription;
import com.example.samuraitravel.entity.User;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
	Subscription findByUser(User user);
}
