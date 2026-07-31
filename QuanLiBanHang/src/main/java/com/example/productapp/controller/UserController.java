package com.example.productapp.controller;

import com.example.productapp.entity.Customer;
import com.example.productapp.entity.Order;
import com.example.productapp.repository.CustomerRepository;
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
    private final CustomerRepository customerRepository;

    public UserController(OrderService orderService, CustomerRepository customerRepository) {
        this.orderService = orderService;
        this.customerRepository = customerRepository;
    }

    /** Xem lịch sử đơn hàng của tôi */
    @GetMapping("/my-orders")
    public String myOrders(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        // Tìm customer theo tên tài khoản hoặc thông tin liên quan (ở đây ví dụ tìm theo username/SĐT)
        String username = authentication.getName();
        Customer customer = customerRepository.findByPhoneNumber(username).orElse(null);

        List<Order> orders;
        if (customer != null) {
            orders = orderService.findOrdersByCustomerId(customer.getId());
        } else {
            orders = List.of();
        }

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

        // 🌟 ĐÃ ĐỔI TỪ order.getUser() SANG order.getCustomer()
        if (order == null || order.getCustomer() == null) {
            return "redirect:/my-orders";
        }

        model.addAttribute("order", order);
        return "my-order-detail";
    }
}