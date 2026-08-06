package com.example.productapp.config;

import com.example.productapp.service.CustomUserDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 💡 Bộ xử lý phân luồng sau khi Đăng nhập thành công
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {

                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

                // Kiểm tra nếu tài khoản có quyền ADMIN -> Chuyển hướng sang /admin
                for (GrantedAuthority authority : authorities) {
                    if (authority.getAuthority().equals("ROLE_ADMIN") || authority.getAuthority().equals("ADMIN")) {
                        response.sendRedirect("/admin");
                        return;
                    }
                }

                // Người dùng thường (ROLE_USER) -> Chuyển hướng sang /products
                response.sendRedirect("/products");
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 🌟 1. MỞ QUYỀN TRUY CẬP CÔNG KHAI CHO SWAGGER UI & API DOCS
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 2. CHỈ DÀNH CHO ADMIN: Thêm, sửa, xóa sản phẩm & trang Dashboard Admin
                        .requestMatchers("/products/add", "/products/edit/**", "/products/delete/**", "/admin/**")
                        .hasAnyAuthority("ADMIN", "ROLE_ADMIN")

                        // 3. BẮT BỘC ĐĂNG NHẬP: Trang xem lịch sử đơn hàng cá nhân
                        .requestMatchers("/my-orders/**").authenticated()

                        // 4. CÔNG KHAI CẢ CHO KHÁCH VẮNG LAI: Xem sản phẩm, xem chi tiết, mua hàng, giỏ hàng, checkout
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/products/**",
                                "/cart/**",
                                "/checkout/**",
                                "/orders/**"
                        ).permitAll()

                        // 5. Các Request còn lại
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customSuccessHandler()) // Phân luồng đăng nhập (Admin -> /admin, User -> /products)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/products") // Đăng xuất xong chuyển ngay tới Trang danh sách mua hàng
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}