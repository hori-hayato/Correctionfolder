
package com.example.samuraitravel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitravel.entity.Role;
import com.example.samuraitravel.entity.Subscription;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.RoleRepository;
import com.example.samuraitravel.repository.SubscriptionRepository;
import com.example.samuraitravel.repository.UserRepository;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, RoleRepository roleRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void create(User user, String customerId, String subscriptionId) {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setCustomerId(customerId);
        subscription.setSubscriptionId(subscriptionId);
        subscriptionRepository.save(subscription);
        Role role = roleRepository.findByName("ROLE_PAID");
        user = userRepository.getReferenceById(user.getId());
        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void delete(User user) {
        Subscription subscription = subscriptionRepository.findByUser(user);
        subscriptionRepository.deleteById(subscription.getId());
        Role role = roleRepository.findByName("ROLE_GENERAL");
        user = userRepository.getReferenceById(user.getId());
        user.setRole(role);
        userRepository.save(user);
    }
}
