package com.example.samuraitravel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.FavoriteRepository;

@Service
public class FavoriteService {
	private final FavoriteRepository favoriteRepository;
	
	public FavoriteService(FavoriteRepository favoriteRepository) {
		this.favoriteRepository = favoriteRepository;
	}	
	
	@Transactional
	public void create(House house, User user) {
		Favorite favorite = new Favorite();
		favorite.setHouse(house);
		favorite.setUser(user);
		favoriteRepository.save(favorite);
	}
	
	@Transactional
	public void delete(House house, User user) {		
		Favorite record = favoriteRepository.findByHouseAndUser(house, user);
		if (record != null) {
			favoriteRepository.deleteById(record.getId());
		}
	}
	
	//お気に入りが投稿済みかどうかをチェックする
		public boolean hasFavorite(House house, User user) {
			if (user == null) {
				return false;
			}
			Favorite favorite = favoriteRepository.findByHouseAndUser(house, user);
			return favorite != null;
		}
	
	

}
