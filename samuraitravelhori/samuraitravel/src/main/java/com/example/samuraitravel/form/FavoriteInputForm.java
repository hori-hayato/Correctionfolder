package com.example.samuraitravel.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteInputForm {
	@NotNull
	private Integer houseId;
	
	@NotNull
	private Integer userId;
	
	public Integer getHouseId() {
		return houseId;
	}
	
	public void setHouseId(Integer houseId) {
		this.houseId = houseId;
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

}