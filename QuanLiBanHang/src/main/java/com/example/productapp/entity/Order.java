package com.example.productapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String phoneNumber;
    private String address;
    private String note;

    // 🌟 ĐÃ ĐỔI TỪ DOUBLE SANG BIGDECIMAL LƯU TỔNG TIỀN
    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount;

    private LocalDateTime orderDate;

    // 🌟 ĐÃ ĐỔI TỪ USER SANG CUSTOMER THEO GÓP Ý CỦA THẦY
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderDetail> orderDetails = new ArrayList<>();
}