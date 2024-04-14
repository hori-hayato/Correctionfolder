package com.example.samuraitravel.form;

import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteRegisterForm {
	@NotNull
	private Integer houseId;
	
	@NotNull
	private Integer userId;
	
	public void setHouseId(Integer houseId) {
		this.houseId = houseId;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	@Transactional
	public void deleteByHouseIdAndUserId(Integer houseId, Integer userId) {
		this.houseId = houseId;
		this.userId = userId;
	}

}