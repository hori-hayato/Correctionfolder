package com.example.samuraitravel.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.Subscription;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.UserEditForm;
import com.example.samuraitravel.repository.SubscriptionRepository;
import com.example.samuraitravel.repository.UserRepository;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.StripeService;
import com.example.samuraitravel.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/user")
public class UserController {
	private final UserRepository userRepository;
    private final UserService userService;
    private final StripeService stripeService;
    private final SubscriptionRepository subscriptionRepository;

    public UserController(UserRepository userRepository, UserService userService, StripeService stripeService, SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.stripeService = stripeService;
        this.subscriptionRepository = subscriptionRepository;
    }   
    
    @GetMapping
    public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {         
        User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());  
        
        model.addAttribute("user", user);
        model.addAttribute("isPaidMember", user.getRole().getName().equals("ROLE_PAID"));
        
        return "user/index";
    }
    
     @GetMapping("/edit")
     public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {        
         User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());  
         UserEditForm userEditForm = new UserEditForm(user.getId(), user.getName(), user.getFurigana(), user.getPostalCode(), user.getAddress(), user.getPhoneNumber(), user.getEmail());
         
         model.addAttribute("userEditForm", userEditForm);
         
         return "user/edit";
     }   
     
     @PostMapping("/update")
     public String update(@ModelAttribute @Validated UserEditForm userEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
         // メールアドレスが変更されており、かつ登録済みであれば、BindingResultオブジェクトにエラー内容を追加する
         if (userService.isEmailChanged(userEditForm) && userService.isEmailRegistered(userEditForm.getEmail())) {
             FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
             bindingResult.addError(fieldError);                       
         }
         
         if (bindingResult.hasErrors()) {
             return "user/edit";
         }
         
         userService.update(userEditForm);
         redirectAttributes.addFlashAttribute("successMessage", "会員情報を編集しました。");
         
         return "redirect:/user";
     }  
     
     @GetMapping("/upgrade")
     public String upgrade(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @RequestParam(name = "session_id", required = false) String sessionId, HttpServletRequest httpServletRequest, Model model, RedirectAttributes redirectAttributes) {
         User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
         if (sessionId == null) {
             String url = stripeService.checkoutSubscription(user, httpServletRequest);
             return "redirect:" + url;
         } else {
             stripeService.subscriptionSuccess(user, sessionId);
             redirectAttributes.addFlashAttribute("successMessage", "有料会員にアップグレードしました。");
             return "redirect:/user";
         }
     }

     @GetMapping("/downgrade")
     public String downgrade(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model, RedirectAttributes redirectAttributes) {
         User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
         Subscription subscription = subscriptionRepository.findByUser(user);
         if (subscription != null) {
             stripeService.deleteCustomer(user, subscription.getCustomerId());
             redirectAttributes.addFlashAttribute("successMessage", "無料会員にダウングレードしました。");
         }
         return "redirect:/user";
     }
 }