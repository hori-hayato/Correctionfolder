package com.example.samuraitravel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewRegisterForm;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.repository.UserRepository;

@Service
public class ReviewService {
	private final HouseRepository houseRepository;
	private final UserRepository userRepository;
	private final ReviewRepository reviewRepository;

public ReviewService(ReviewRepository reviewRepository, HouseRepository houseRepository, UserRepository userRepository) {
	this.houseRepository = houseRepository;
	this.userRepository = userRepository;
	this.reviewRepository = reviewRepository;
}

@Transactional
public void create(User user, ReviewRegisterForm reviewRegisterForm) {
	Review review = new Review();
	
	House house = houseRepository.getReferenceById(reviewRegisterForm.getHouseId());
	review.setHouse(house);
	review.setUser(user);
	review.setRating(reviewRegisterForm.getRating());
	review.setComment(reviewRegisterForm.getComment());
	reviewRepository.save(review);
	}

@Transactional
public void update(ReviewEditForm reviewEditForm) {
	Review review = reviewRepository.getReferenceById(reviewEditForm.getId());
	review.setRating(reviewEditForm.getRating());
	review.setComment(reviewEditForm.getComment());
	reviewRepository.save(review);
	}

//レビューが投稿済みかどうかをチェックする
	public boolean hasReview(House house, User user) {
		if (user == null) {
			return false;
		}
		Review review = reviewRepository.findByHouseAndUser(house, user);
		return review != null;
	}
}