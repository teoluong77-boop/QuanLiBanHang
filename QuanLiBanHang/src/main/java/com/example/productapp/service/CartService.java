package com.example.productapp.service;

import com.example.productapp.entity.CartItem; // <- Đã đổi sang package entity
import com.example.productapp.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;

@Service
@SessionScope // Lưu giỏ hàng trong Session của từng người dùng
public class CartService {

    private final List<CartItem> items = new ArrayList<>();

    // Thêm sản phẩm vào giỏ
    public void addProduct(Product product, int quantity) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(product, quantity));
    }

    // Xóa sản phẩm khỏi giỏ
    public void removeProduct(Long productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    // Cập nhật số lượng
    public void updateQuantity(Long productId, int quantity) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                if (quantity <= 0) {
                    removeProduct(productId);
                } else {
                    item.setQuantity(quantity);
                }
                return;
            }
        }
    }

    // Lấy danh sách món trong giỏ
    public List<CartItem> getItems() {
        return items;
    }

    // Tính tổng số tiền trong giỏ
    public double getTotalAmount() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    // Xóa sạch giỏ hàng
    public void clear() {
        items.clear();
    }
}