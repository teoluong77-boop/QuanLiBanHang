package com.example.productapp.controller;

import com.example.productapp.entity.CartItem;
import com.example.productapp.entity.Product;
import com.example.productapp.service.CartService;
import com.example.productapp.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@Tag(name = "Cart Controller", description = "Quản lý Giỏ hàng (Xem, Thêm, Cập nhật, Xóa món)")
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    public CartController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    // =========================================================================
    // 🌐 1. TRẢ VỀ GIAO DIỆN WEB THYMELEAF (Dành cho trình duyệt lướt Web)
    // =========================================================================

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

    // =========================================================================
    // 🚀 2. TRẢ VỀ JSON CHO SWAGGER (Có @ResponseBody để Swagger hiển thị)
    // =========================================================================

    @GetMapping("/api/items")
    @ResponseBody
    @Operation(summary = "Lấy thông tin các sản phẩm trong giỏ hàng và tổng tiền (JSON)")
    public Map<String, Object> getCartItemsApi() {
        Map<String, Object> response = new HashMap<>();
        Collection<CartItem> items = cartService.getItems();
        BigDecimal totalAmount = cartService.getTotalAmount();

        response.put("cartItems", items);
        response.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        return response;
    }

    @PostMapping("/api/add/{id}")
    @ResponseBody
    @Operation(summary = "Thêm sản phẩm vào giỏ hàng qua API")
    public ResponseEntity<String> addToCartApi(@PathVariable("id") Long id,
                                               @RequestParam(name = "quantity", defaultValue = "1") int quantity) {
        Product product = productService.findById(id);
        if (product == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy sản phẩm với ID: " + id);
        }
        cartService.addProduct(product, quantity);
        return ResponseEntity.ok("Đã thêm sản phẩm [" + product.getName() + "] với số lượng " + quantity + " vào giỏ.");
    }

    @PutMapping("/api/update/{id}")
    @ResponseBody
    @Operation(summary = "Cập nhật số lượng món trong giỏ qua API")
    public ResponseEntity<String> updateQuantityApi(@PathVariable("id") Long id,
                                                    @RequestParam("quantity") int quantity) {
        cartService.updateQuantity(id, quantity);
        return ResponseEntity.ok("Đã cập nhật số lượng cho món ID " + id + " thành " + quantity);
    }

    @DeleteMapping("/api/remove/{id}")
    @ResponseBody
    @Operation(summary = "Xóa món khỏi giỏ hàng qua API")
    public ResponseEntity<String> removeFromCartApi(@PathVariable("id") Long id) {
        cartService.removeProduct(id);
        return ResponseEntity.ok("Đã xóa sản phẩm ID " + id + " khỏi giỏ hàng.");
    }
}