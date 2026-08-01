package com.example.productapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "email")
    private String email;

    // 🌟 LIÊN KẾT 1-1 VỚI TÀI KHOẢN USER ĐỂ BIẾT VÍ TIỀN CỦA AI
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 🌟 VÍ TIỀN KHÁCH HÀNG (Mặc định 0 VNĐ)
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    // Một khách hàng có thể có nhiều đơn hàng
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
}