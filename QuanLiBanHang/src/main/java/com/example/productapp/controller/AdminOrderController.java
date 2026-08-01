package com.example.productapp.controller;

import com.example.productapp.entity.Order;
import com.example.productapp.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

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
}