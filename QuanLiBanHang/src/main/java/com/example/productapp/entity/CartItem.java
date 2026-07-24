package com.example.productapp.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    private Product product;
    private int quantity;

    // Tính tổng tiền cho item này (Số lượng * Giá)
    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }
}