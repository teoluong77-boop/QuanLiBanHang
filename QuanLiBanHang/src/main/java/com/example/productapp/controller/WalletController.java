package com.example.productapp.controller;

import com.example.productapp.entity.Customer;
import com.example.productapp.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/wallet")
@Tag(name = "Wallet Controller", description = "Quản lý Ví tiền cá nhân và Nạp tiền")
public class WalletController {

    @Autowired
    private CustomerService customerService;

    // =========================================================================
    // 🌐 1. TRẢ VỀ GIAO DIỆN WEB THYMELEAF (Dành cho trình duyệt lướt Web)
    // =========================================================================

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

    // =========================================================================
    // 🚀 2. TRẢ VỀ JSON CHO SWAGGER (Có @ResponseBody để Swagger hiển thị)
    // =========================================================================

    @GetMapping("/api/info")
    @ResponseBody
    @Operation(summary = "Lấy thông tin số dư ví tiền cá nhân dạng JSON")
    public ResponseEntity<?> getWalletInfoApi(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập để xem thông tin ví tiền!");
        }

        Customer customer = customerService.getCustomerByUsername(userDetails.getUsername());
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy thông tin khách hàng!");
        }

        return ResponseEntity.ok(customer);
    }

    @PostMapping("/api/deposit")
    @ResponseBody
    @Operation(summary = "Nạp tiền vào ví cá nhân qua REST API")
    public ResponseEntity<?> depositApi(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestParam("amount") BigDecimal amount) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập trước khi nạp tiền!");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Số tiền nạp phải lớn hơn 0!");
        }

        Customer customer = customerService.getCustomerByUsername(userDetails.getUsername());
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy thông tin khách hàng!");
        }

        customerService.deposit(customer.getId(), amount);

        // Lấy lại thông tin khách hàng đã cập nhật số dư mới
        Customer updatedCustomer = customerService.getCustomerByUsername(userDetails.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Nạp tiền vào ví thành công!");
        response.put("depositedAmount", amount);
        response.put("newBalance", updatedCustomer != null ? updatedCustomer.getBalance() : null);

        return ResponseEntity.ok(response);
    }
}