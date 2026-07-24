package com.example.productapp.controller;

import com.example.productapp.entity.Order;
import com.example.productapp.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class UserController {

    private final OrderService orderService;

    public UserController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Xem lịch sử đơn hàng của tôi */
    @GetMapping("/my-orders")
    public String myOrders(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        List<Order> orders = orderService.findOrdersByUsername(username);
        model.addAttribute("orders", orders);
        return "my-orders";
    }

    /** Xem chi tiết 1 đơn hàng của tôi */
    @GetMapping("/my-orders/{id}")
    public String myOrderDetail(@PathVariable("id") Long id, Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        Order order = orderService.findOrderById(id);
        if (order == null || order.getUser() == null || !order.getUser().getUsername().equals(authentication.getName())) {
            return "redirect:/my-orders";
        }
        model.addAttribute("order", order);
        return "my-order-detail";
    }
}