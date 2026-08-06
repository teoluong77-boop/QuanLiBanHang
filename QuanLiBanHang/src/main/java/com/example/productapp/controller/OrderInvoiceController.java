package com.example.productapp.controller;

import com.example.productapp.entity.Order;
import com.example.productapp.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Tag(name = "Order Invoice Controller", description = "Quản lý và xuất thông tin Hóa đơn (Invoice)")
public class OrderInvoiceController {

    private final OrderService orderService;

    public OrderInvoiceController(OrderService orderService) {
        this.orderService = orderService;
    }

    // =========================================================================
    // 🌐 1. TRẢ VỀ GIAO DIỆN WEB THYMELEAF (Dành cho trình duyệt lướt Web/In ấn)
    // =========================================================================

    /** Hiển thị trang Hóa đơn để In / Xuất PDF */
    @GetMapping("/orders/{id}/invoice")
    public String showInvoice(@PathVariable("id") Long id, Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        Order order = orderService.findOrderById(id);
        if (order == null) {
            return "redirect:/";
        }

        // Kiểm tra quyền: Hoặc là Admin, hoặc là đơn hàng hợp lệ
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Nếu không phải Admin, kiểm tra xem đơn hàng có gắn với Customer hay không
        if (!isAdmin && order.getCustomer() == null) {
            return "redirect:/";
        }

        model.addAttribute("order", order);
        return "invoice";
    }

    // =========================================================================
    // 🚀 2. TRẢ VỀ JSON CHO SWAGGER (Có @ResponseBody để Swagger hiển thị)
    // =========================================================================

    @GetMapping("/api/orders/{id}/invoice")
    @ResponseBody
    @Operation(summary = "Lấy dữ liệu chi tiết hóa đơn dưới dạng JSON")
    public ResponseEntity<?> getInvoiceDataApi(@PathVariable("id") Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập trước khi xem hóa đơn!");
        }

        Order order = orderService.findOrderById(id);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy đơn hàng với ID: " + id);
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && order.getCustomer() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền truy cập hóa đơn này!");
        }

        return ResponseEntity.ok(order);
    }
}