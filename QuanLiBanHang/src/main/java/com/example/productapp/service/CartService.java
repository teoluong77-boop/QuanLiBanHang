package com.example.productapp.service;

import com.example.productapp.entity.CartItem;
import com.example.productapp.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
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

    // 🌟 TÍNH TỔNG SỐ TIỀN TRONG GIỎ (ĐÃ ĐỔI SANG BIGDECIMAL)
    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            if (item.getProduct() != null && item.getProduct().getPrice() != null) {
                BigDecimal itemPrice = item.getProduct().getPrice();
                BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    // Xóa sạch giỏ hàng
    public void clear() {
        items.clear();
    }
}