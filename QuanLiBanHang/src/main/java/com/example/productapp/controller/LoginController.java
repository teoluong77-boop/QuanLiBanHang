package com.example.productapp.controller;

import com.example.productapp.entity.User;
import com.example.productapp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

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

    // --- BỔ SUNG: Xử lý Đăng nhập thủ công và Phân luồng Admin ---
    @PostMapping("/login")
    public String loginUser(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            HttpSession session,
                            Model model) {

        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Kiểm tra mật khẩu khớp với mã hóa BCrypt
            if (passwordEncoder.matches(password, user.getPassword())) {
                session.setAttribute("currentUser", user);

                // PHÂN LUỒNG TẠI ĐÂY:
                if ("ROLE_ADMIN".equalsIgnoreCase(user.getRole()) || "ADMIN".equalsIgnoreCase(user.getRole())) {
                    return "redirect:/admin"; // Admin -> Chui thẳng vào Admin Dashboard mới
                } else {
                    return "redirect:/products"; // User -> Vào danh sách sản phẩm
                }
            }
        }

        model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không chính xác!");
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
                .password(passwordEncoder.encode(password))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
        return "redirect:/login?registered";
    }
}