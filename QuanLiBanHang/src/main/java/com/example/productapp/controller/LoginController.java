package com.example.productapp.controller;

import com.example.productapp.entity.User;
import com.example.productapp.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@Tag(name = "Login & Register Controller", description = "Quản lý Đăng nhập, Đăng ký và Phân quyền người dùng")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =========================================================================
    // 🌐 1. TRẢ VỀ GIAO DIỆN WEB THYMELEAF (Dành cho trình duyệt lướt Web)
    // =========================================================================

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

    // =========================================================================
    // 🚀 2. TRẢ VỀ JSON CHO SWAGGER (Có @ResponseBody để Swagger hiển thị)
    // =========================================================================

    @PostMapping("/api/login")
    @ResponseBody
    @Operation(summary = "Xác thực Đăng nhập qua REST API")
    public ResponseEntity<?> loginUserApi(@RequestParam("username") String username,
                                          @RequestParam("password") String password,
                                          HttpSession session) {

        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                session.setAttribute("currentUser", user);

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Đăng nhập thành công!");
                response.put("username", user.getUsername());
                response.put("role", user.getRole());

                if ("ROLE_ADMIN".equalsIgnoreCase(user.getRole()) || "ADMIN".equalsIgnoreCase(user.getRole())) {
                    response.put("redirectUrl", "/admin");
                } else {
                    response.put("redirectUrl", "/products");
                }

                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Tên đăng nhập hoặc mật khẩu không chính xác!"));
    }

    @PostMapping("/api/register")
    @ResponseBody
    @Operation(summary = "Đăng ký tài khoản người dùng mới qua REST API")
    public ResponseEntity<?> registerUserApi(@RequestParam("username") String username,
                                             @RequestParam("password") String password) {

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tên tài khoản đã tồn tại!"));
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tạo tài khoản thành công!");
        response.put("username", user.getUsername());
        response.put("role", user.getRole());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}