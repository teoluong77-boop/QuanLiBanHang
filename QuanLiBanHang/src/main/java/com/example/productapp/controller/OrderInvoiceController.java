package com.example.productapp.controller;

import com.example.productapp.entity.Order;
import com.example.productapp.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OrderInvoiceController {

    private final OrderService orderService;

    public OrderInvoiceController(OrderService orderService) {
        this.orderService = orderService;
    }

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
}