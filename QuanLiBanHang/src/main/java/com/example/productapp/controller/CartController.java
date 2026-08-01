package com.example.productapp.controller;

import com.example.productapp.entity.Product;
import com.example.productapp.service.CartService;
import com.example.productapp.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    public CartController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    /** Xem giỏ hàng */
    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cartItems", cartService.getItems());
        model.addAttribute("totalAmount", cartService.getTotalAmount());
        return "cart";
    }

    /** Thêm vào giỏ hàng (Sau khi thêm sẽ tự ở lại trang cũ để chọn tiếp món khác) */
    @PostMapping("/add/{id}")
    public String addToCart(@PathVariable("id") Long id,
                            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                            @RequestHeader(value = "Referer", required = false) String referer) {
        Product product = productService.findById(id);
        if (product != null) {
            cartService.addProduct(product, quantity);
        }
        // Quay lại đúng trang khách đang đứng (trang sản phẩm) thay vì bắt nhảy ngay vào giỏ
        return "redirect:" + (referer != null ? referer : "/products");
    }

    /** Cập nhật số lượng */
    @PostMapping("/update/{id}")
    public String updateQuantity(@PathVariable("id") Long id,
                                 @RequestParam("quantity") int quantity) {
        cartService.updateQuantity(id, quantity);
        return "redirect:/cart";
    }

    /** Xóa 1 món khỏi giỏ */
    @PostMapping("/remove/{id}")
    public String removeFromCart(@PathVariable("id") Long id) {
        cartService.removeProduct(id);
        return "redirect:/cart";
    }
}