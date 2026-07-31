package com.example.productapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Entity
@Table(name = "order_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @OnDelete(action = OnDeleteAction.CASCADE) // 🌟 TỰ ĐỘNG XÓA ORDER_DETAIL KHI SẢN PHẨM BỊ XÓA
    private Product product;

    private Integer quantity;

    // 🌟 ĐÃ ĐỔI SANG BIGDECIMAL LƯU ĐƠN GIÁ TẠI THỜI ĐIỂM MUA
    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    // 🌟 THÊM TRƯỜNG THÀNH TIỀN (subtotal = price * quantity) Theo ý thầy dặn
    @Column(precision = 15, scale = 2)
    private BigDecimal subtotal;
}