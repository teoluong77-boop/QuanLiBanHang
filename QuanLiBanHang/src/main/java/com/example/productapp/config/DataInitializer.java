package com.example.productapp.config;

import com.example.productapp.entity.Category;
import com.example.productapp.entity.User;
import com.example.productapp.repository.CategoryRepository;
import com.example.productapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, CategoryRepository categoryRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Tao Admin
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("123456"))
                    .role("ROLE_ADMIN")
                    .build();
            userRepository.save(admin);
            System.out.println(">>> Đã tạo tài khoản Admin: admin / 123456");
        }

        // Tao Danh muc mau
        if (categoryRepository.count() == 0) {
            categoryRepository.save(Category.builder().name("Điện thoại & Máy tính").build());
            categoryRepository.save(Category.builder().name("Thời trang & Phụ kiện").build());
            categoryRepository.save(Category.builder().name("Đồ gia dụng").build());
            System.out.println(">>> Đã tạo các Danh mục mẫu!");
        }
    }
}