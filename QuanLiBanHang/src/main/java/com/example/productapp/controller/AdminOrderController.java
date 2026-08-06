package com.example.productapp.controller;

import com.example.productapp.entity.Order;
import com.example.productapp.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/orders")
@Tag(name = "Admin Order Controller", description = "Quản lý đơn hàng và xác nhận giao hàng dành cho Admin")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // =========================================================================
    // 🌐 1. TRẢ VỀ GIAO DIỆN WEB THYMELEAF (Dành cho trình duyệt lướt Web)
    // =========================================================================

    /** Danh sách tất cả đơn hàng & thống kê doanh thu cho Admin */
    @GetMapping
    public String listOrders(Model model) {
        List<Order> orders = orderService.findAllOrders();
        BigDecimal totalRevenue = orderService.getTotalRevenue();

        model.addAttribute("orders", orders);
        model.addAttribute("totalRevenue", totalRevenue);
        return "admin-orders";
    }

    /** Xem chi tiết 1 đơn hàng */
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable("id") Long id, Model model) {
        Order order = orderService.findOrderById(id);
        if (order == null) {
            return "redirect:/admin/orders";
        }
        model.addAttribute("order", order);
        return "admin-order-detail";
    }

    /** 🌟 XỬ LÝ BẤM NÚT XÁC NHẬN ĐÃ GIAO HÀNG (CỘNG TIỀN COD VÀO VÍ ADMIN) */
    @PostMapping("/confirm/{id}")
    public String confirmOrderDelivery(@PathVariable("id") Long id) {
        orderService.confirmOrderDelivery(id);
        return "redirect:/admin/orders";
    }

    /** 🌟 XỬ LÝ BẤM NÚT XÓA ĐƠN HÀNG */
    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable("id") Long id) {
        orderService.deleteOrderById(id);
        return "redirect:/admin/orders";
    }

    // =========================================================================
    // 🚀 2. TRẢ VỀ JSON CHO SWAGGER (Có @ResponseBody để Swagger quét được)
    // =========================================================================

    @GetMapping("/api/all")
    @ResponseBody
    @Operation(summary = "Lấy danh sách tất cả đơn hàng và tổng doanh thu (JSON)")
    public Map<String, Object> getAllOrdersApi() {
        Map<String, Object> response = new HashMap<>();
        List<Order> orders = orderService.findAllOrders();
        BigDecimal totalRevenue = orderService.getTotalRevenue();

        response.put("orders", orders);
        response.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        return response;
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    @Operation(summary = "Lấy thông tin chi tiết của 1 đơn hàng theo ID (JSON)")
    public ResponseEntity<Order> getOrderDetailApi(@PathVariable("id") Long id) {
        Order order = orderService.findOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping("/api/confirm/{id}")
    @ResponseBody
    @Operation(summary = "Xác nhận đã giao hàng thành công qua API")
    public ResponseEntity<String> confirmOrderDeliveryApi(@PathVariable("id") Long id) {
        orderService.confirmOrderDelivery(id);
        return ResponseEntity.ok("Xác nhận giao hàng thành công cho đơn hàng ID: " + id);
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    @Operation(summary = "Xóa đơn hàng theo ID qua API")
    public ResponseEntity<String> deleteOrderApi(@PathVariable("id") Long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.ok("Đã xóa đơn hàng ID: " + id);
    }
}