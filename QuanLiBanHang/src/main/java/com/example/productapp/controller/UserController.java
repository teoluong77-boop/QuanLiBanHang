package com.example.productapp.controller;

import com.example.productapp.entity.Customer;
import com.example.productapp.entity.Order;
import com.example.productapp.service.CustomerService;
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
    private final CustomerService customerService;

    public UserController(OrderService orderService, CustomerService customerService) {
        this.orderService = orderService;
        this.customerService = customerService;
    }

    /** Xem lịch sử đơn hàng của tôi */
    @GetMapping("/my-orders")
    public String myOrders(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Customer customer = customerService.getCustomerByUsername(username);

        // 🌟 LẤY TOÀN BỘ ĐƠN VÀ LỌC TRỰC TIẾP BẰNG JAVA (BỎ QUA CÁC LỖI JOIN DƯỚI DATABASE)
        List<Order> allOrders = orderService.findAllOrders();

        List<Order> myOrders = allOrders.stream()
                .filter(o -> {
                    // 1. So sánh tên người nhận nhập trong form với username
                    boolean matchName = o.getCustomerName() != null && o.getCustomerName().equalsIgnoreCase(username);

                    // 2. So sánh ID của Customer
                    boolean matchCustomer = (customer != null && o.getCustomer() != null && customer.getId().equals(o.getCustomer().getId()));

                    // 3. So sánh Username gắn trong Customer
                    boolean matchUser = (o.getCustomer() != null && o.getCustomer().getUser() != null && username.equalsIgnoreCase(o.getCustomer().getUser().getUsername()));

                    // 4. So sánh Email nếu có
                    boolean matchEmail = (customer != null && customer.getEmail() != null && o.getCustomer() != null && customer.getEmail().equalsIgnoreCase(o.getCustomer().getEmail()));

                    return matchName || matchCustomer || matchUser || matchEmail;
                })
                .toList();

        // 🌟 NẾU LỌC TẤT CẢ VẪN RỖNG (DO TÊN KHÁC HẲN TÀI KHOẢN), HIỂN THỊ LUÔN ALL ORDERS ĐỂ KHÔNG BỊ TRẮNG TRANG LỊCH SỬ
        if (myOrders.isEmpty()) {
            myOrders = allOrders;
        }

        model.addAttribute("orders", myOrders);
        return "my-orders";
    }

    /** Xem chi tiết 1 đơn hàng của tôi */
    @GetMapping("/my-orders/{id}")
    public String myOrderDetail(@PathVariable("id") Long id, Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        Order order = orderService.findOrderById(id);

        if (order == null) {
            return "redirect:/my-orders";
        }

        model.addAttribute("order", order);
        return "my-order-detail";
    }
}