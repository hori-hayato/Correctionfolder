package com.example.samuraitravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.FavoriteRegisterForm;
import com.example.samuraitravel.repository.FavoriteRepository;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.FavoriteService;

import jakarta.servlet.http.HttpSession;

@Controller
public class FavoriteController {
	private final FavoriteRepository favoriteRepository;
	private final HouseRepository houseRepository;
	private final FavoriteService favoriteService;
	
	public FavoriteController(FavoriteRepository favoriteRepository, HouseRepository houseRepository, FavoriteService favoriteService) {
		this.favoriteRepository = favoriteRepository;
		this.houseRepository = houseRepository;
		this.favoriteService = favoriteService;
	}
	
	@GetMapping("/favorites")
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC)Pageable pageable, Model model, HttpSession httpSession) {
		User user = userDetailsImpl.getUser();
		Page<Favorite> favoritesPage = favoriteRepository.findByUser(user, pageable);
		httpSession.setAttribute("favoriteHouse", houseRepository.findAll());
		
		model.addAttribute("user", user);
		model.addAttribute("favoritesPage", favoritesPage);
		
		return "favorites/index";
	}
	
	@PostMapping("/houses/{id}/create")
	public String create(@ModelAttribute FavoriteRegisterForm favoriteRegisterForm, @PathVariable(name = "id") Integer id, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
		House house = houseRepository.getReferenceById(id);
		User user = userDetailsImpl.getUser();
		
		favoriteService.create(favoriteRegisterForm, house, user);
		
		model.addAttribute("favoriteRegisterForm", favoriteRegisterForm);
		
		return "redirect:/houses/{id}";
	}
	
	@PostMapping("houses/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, FavoriteRegisterForm favoriteRegisterForm, RedirectAttributes redirectAttributes, Model model) {
		Integer userId = userDetailsImpl.getUser().getId();
		favoriteRepository.deleteByHouseIdAndUserId(id, userId);
		favoriteRegisterForm.deleteByHouseIdAndUserId(id, userId);
		
		model.addAttribute("favoriteRegisterForm", favoriteRegisterForm);
		
		return "redirect:/houses/{id}";
	}

}
