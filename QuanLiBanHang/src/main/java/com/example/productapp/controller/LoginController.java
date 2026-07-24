package com.example.productapp.controller;

import com.example.productapp.entity.User;
import com.example.productapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model) {
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Tên tài khoản đã tồn tại!");
            return "register";
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password)) // Mã hóa mật khẩu BCrypt trước khi lưu CSDL
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
        return "redirect:/login?registered";
    }
}