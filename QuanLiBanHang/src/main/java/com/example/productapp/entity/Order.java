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

    // 🌟 THÊM FIELD EMAIL ĐỂ KHÔNG BỊ LỖI LÚC BUILDER() GỌI ORDER
    private String email;

    // 🌟 PHƯƠNG THỨC THANH TOÁN ("COD" hoặc "WALLET")
    private String paymentMethod;

    // 🌟 TRẠNG THÁI ĐƠN HÀNG ("PENDING": Đang xử lý, "COMPLETED": Đã giao/Đã thanh toán)
    private String status;

    // 🌟 CỜ XÓA MỀM (true: Đã xóa/ẩn, false: Đang hiển thị)
    @Builder.Default
    private Boolean deleted = false;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount;

    private LocalDateTime orderDate;

    // 🌟 CHUYỂN SANG EAGER ĐỂ TRÁNH LỖI LazyInitializationException LÀM ROLLBACK CSDL
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderDetail> orderDetails = new ArrayList<>();
}