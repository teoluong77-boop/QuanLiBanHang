package com.example.productapp.controller;

import com.example.productapp.entity.Customer;
import com.example.productapp.entity.Order;
import com.example.productapp.service.CartService;
import com.example.productapp.service.CustomerService;
import com.example.productapp.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/checkout")
@Tag(name = "Checkout Controller", description = "Xử lý Thanh toán và Tạo Đơn hàng")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final CustomerService customerService;

    public CheckoutController(CartService cartService,
                              OrderService orderService,
                              CustomerService customerService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.customerService = customerService;
    }

    // =========================================================================
    // 🌐 1. TRẢ VỀ GIAO DIỆN WEB THYMELEAF (Dành cho trình duyệt lướt Web)
    // =========================================================================

    /** Hiển thị trang nhập thông tin đặt hàng */
    @GetMapping
    public String showCheckoutPage(Authentication authentication, Model model) {
        if (cartService.getItems() == null || cartService.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cartItems", cartService.getItems());
        model.addAttribute("totalAmount", cartService.getTotalAmount());

        if (authentication != null) {
            Customer customer = customerService.getCustomerByUsername(authentication.getName());
            model.addAttribute("customer", customer);
        }

        return "checkout";
    }

    /** Xử lý bấm nút Đặt hàng */
    @PostMapping
    public String processCheckout(@RequestParam("customerName") String customerName,
                                  @RequestParam("phoneNumber") String phoneNumber,
                                  @RequestParam("address") String address,
                                  @RequestParam(value = "note", required = false) String note,
                                  @RequestParam(value = "paymentMethod", defaultValue = "COD") String paymentMethod,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {

        if (cartService.getItems() == null || cartService.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        String username = (authentication != null) ? authentication.getName() : null;

        // 🌟 Lấy email thật từ Customer nếu có, tránh tạo chuỗi ảo gây lỗi DB
        String email = "";
        if (username != null) {
            Customer existingCustomer = customerService.getCustomerByUsername(username);
            if (existingCustomer != null && existingCustomer.getEmail() != null) {
                email = existingCustomer.getEmail();
            }
        }

        try {
            Order order = orderService.createOrder(customerName, phoneNumber, address, note, email, paymentMethod, username);

            if (order == null) {
                return "redirect:/cart";
            }

            redirectAttributes.addFlashAttribute("order", order);
            return "redirect:/checkout/success";

        } catch (Exception e) {
            // 🌟 IN LỖI RA CONSOLE ĐỂ DỄ BẮT BỆNH NẾU DÍNH ROLLBACK
            System.err.println("❌ LỖI TẠO ĐƠN HÀNG: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Có lỗi xảy ra trong quá trình xử lý đơn hàng!");
            return "redirect:/checkout";
        }
    }

    /** TRANG HIỂN THỊ ĐẶT HÀNG THÀNH CÔNG */
    @GetMapping("/success")
    public String orderSuccess() {
        return "order-success";
    }

    // =========================================================================
    // 🚀 2. TRẢ VỀ JSON CHO SWAGGER (Có @ResponseBody để Swagger hiển thị)
    // =========================================================================

    @GetMapping("/api/summary")
    @ResponseBody
    @Operation(summary = "Lấy tóm tắt thông tin thanh toán (Giỏ hàng & Khách hàng đăng nhập)")
    public ResponseEntity<Map<String, Object>> getCheckoutSummaryApi(Authentication authentication) {
        if (cartService.getItems() == null || cartService.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Giỏ hàng hiện đang trống!"));
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("cartItems", cartService.getItems());
        summary.put("totalAmount", cartService.getTotalAmount() != null ? cartService.getTotalAmount() : BigDecimal.ZERO);

        if (authentication != null) {
            Customer customer = customerService.getCustomerByUsername(authentication.getName());
            summary.put("customer", customer);
        }

        return ResponseEntity.ok(summary);
    }

    @PostMapping("/api/process")
    @ResponseBody
    @Operation(summary = "Xử lý đặt hàng trực tiếp từ Swagger / REST API")
    public ResponseEntity<?> processCheckoutApi(@RequestParam("customerName") String customerName,
                                                @RequestParam("phoneNumber") String phoneNumber,
                                                @RequestParam("address") String address,
                                                @RequestParam(value = "note", required = false) String note,
                                                @RequestParam(value = "paymentMethod", defaultValue = "COD") String paymentMethod,
                                                Authentication authentication) {

        if (cartService.getItems() == null || cartService.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("Không thể tạo đơn hàng vì giỏ hàng trống!");
        }

        String username = (authentication != null) ? authentication.getName() : null;
        String email = "";
        if (username != null) {
            Customer existingCustomer = customerService.getCustomerByUsername(username);
            if (existingCustomer != null && existingCustomer.getEmail() != null) {
                email = existingCustomer.getEmail();
            }
        }

        try {
            Order order = orderService.createOrder(customerName, phoneNumber, address, note, email, paymentMethod, username);
            if (order == null) {
                return ResponseEntity.internalServerError().body("Không thể tạo đơn hàng!");
            }
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo đơn hàng: " + e.getMessage());
        }
    }
}