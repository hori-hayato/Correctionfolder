package com.example.samuraitravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReservationInputForm;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewRegisterForm;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.ReviewService;

@Controller
@RequestMapping("/houses/{houseId}/reviews")
public class ReviewController {
  private final ReviewRepository reviewRepository;
  private final ReviewService reviewService;
  private final HouseRepository houseRepository;
  
  public ReviewController(ReviewRepository reviewRepository,ReviewService reviewService, HouseRepository houseRepository) {
    this.reviewRepository = reviewRepository;
    this.reviewService = reviewService;
    this.houseRepository = houseRepository;
  }
  
  @GetMapping
  public String index(@PathVariable(name = "houseId") Integer houseId,
		  @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
  			Model model) {
	  House house = houseRepository.getReferenceById(houseId);
	   Page<Review> reviewPage = reviewRepository.findByHouseOrderByUpdatedAtDesc(house, pageable);
	   model.addAttribute("reviewPage", reviewPage);
	   model.addAttribute("house", house);
	   return "reviews/index";
  }
  
  @GetMapping("/register")
  public String register(@PathVariable(name = "houseId") Integer houseId, Model model) {
      House house = houseRepository.getReferenceById(houseId);
      model.addAttribute("house", house);
      model.addAttribute("reviewRegisterForm", new ReviewRegisterForm());
      return "reviews/register";
  }


  @PostMapping("/create")
  public String create(@PathVariable(name = "houseId") Integer houseId, @ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm, BindingResult bindingResult,
		  RedirectAttributes redirectAttributes,
		  @AuthenticationPrincipal UserDetailsImpl userDetailsImpl
		  ) {
      if (bindingResult.hasErrors()) {
          return "reviews/register";
      }
      User user = userDetailsImpl.getUser();
      reviewService.create(user, reviewRegisterForm);
 
      redirectAttributes.addFlashAttribute("successMessage", "レビューを登録しました。");
      return "redirect:/houses/{houseId}";
  }

  @GetMapping("/{id}/edit")
  public String edit(@PathVariable(name = "houseId") Integer houseId,
		  			@PathVariable(name = "id") Integer id,
		             @ModelAttribute ReservationInputForm reservationInputForm,
		             @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
		             Model model) {
	  House house = houseRepository.getReferenceById(houseId);
	  User user = userDetailsImpl.getUser();
      Review review = reviewRepository.getReferenceById(id);
      ReviewEditForm reviewEditForm = new ReviewEditForm(review.getId(),house.getId(),user.getId(),review.getRating(), review.getComment());
      model.addAttribute("house", house);
      model.addAttribute("reviewEditForm", reviewEditForm);
      model.addAttribute("review", review);
      return "reviews/edit";
  }

  
  @PostMapping("/{id}/update")
  public String update(@PathVariable(name = "houseId") Integer houseId, @ModelAttribute@Validated ReviewEditForm reviewEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
	   if(bindingResult.hasErrors()) {
		   return "reviews/edit";
	   }
	   reviewService.update(reviewEditForm);
	   redirectAttributes.addFlashAttribute("successMessage", "レビューを編集しました。");
	   return "redirect:/houses/{houseId}";

  }
  
  @PostMapping("/{id}/delete")
  public String delete(@PathVariable(name = "houseId") Integer houseId, @PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
	   reviewRepository.deleteById(id);
	   redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");
	   return "redirect:/houses/{houseId}";
  }
  
}