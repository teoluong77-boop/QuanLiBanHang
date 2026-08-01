package com.example.productapp.controller;

import com.example.productapp.entity.Customer;
import com.example.productapp.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private CustomerService customerService;

    /** Hiển thị trang ví tiền cá nhân */
    @GetMapping
    public String showWallet(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Customer customer = customerService.getCustomerByUsername(userDetails.getUsername());
        model.addAttribute("customer", customer);
        return "wallet";
    }

    /** Xử lý nạp tiền */
    @PostMapping("/deposit")
    public String deposit(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam("amount") BigDecimal amount) {
        if (userDetails != null) {
            Customer customer = customerService.getCustomerByUsername(userDetails.getUsername());
            if (customer != null) {
                customerService.deposit(customer.getId(), amount);
            }
        }
        return "redirect:/wallet?success";
    }
}