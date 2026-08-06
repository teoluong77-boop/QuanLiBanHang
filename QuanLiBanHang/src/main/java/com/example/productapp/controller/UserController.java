package com.example.productapp.controller;

import com.example.productapp.entity.Customer;
import com.example.productapp.entity.Order;
import com.example.productapp.service.CustomerService;
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

import java.util.List;

@Controller
@Tag(name = "User Controller", description = "Quản lý thông tin cá nhân và Lịch sử đơn hàng của Người dùng")
public class UserController {

    private final OrderService orderService;
    private final CustomerService customerService;

    public UserController(OrderService orderService, CustomerService customerService) {
        this.orderService = orderService;
        this.customerService = customerService;
    }

    // =========================================================================
    // 🌐 1. TRẢ VỀ GIAO DIỆN WEB THYMELEAF (Dành cho trình duyệt lướt Web)
    // =========================================================================

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

    // =========================================================================
    // 🚀 2. TRẢ VỀ JSON CHO SWAGGER (Có @ResponseBody để Swagger hiển thị)
    // =========================================================================

    @GetMapping("/api/my-orders")
    @ResponseBody
    @Operation(summary = "Lấy danh sách lịch sử đơn hàng cá nhân dạng JSON")
    public ResponseEntity<?> getMyOrdersApi(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập trước khi xem lịch sử đơn hàng!");
        }

        String username = authentication.getName();
        Customer customer = customerService.getCustomerByUsername(username);
        List<Order> allOrders = orderService.findAllOrders();

        List<Order> myOrders = allOrders.stream()
                .filter(o -> {
                    boolean matchName = o.getCustomerName() != null && o.getCustomerName().equalsIgnoreCase(username);
                    boolean matchCustomer = (customer != null && o.getCustomer() != null && customer.getId().equals(o.getCustomer().getId()));
                    boolean matchUser = (o.getCustomer() != null && o.getCustomer().getUser() != null && username.equalsIgnoreCase(o.getCustomer().getUser().getUsername()));
                    boolean matchEmail = (customer != null && customer.getEmail() != null && o.getCustomer() != null && customer.getEmail().equalsIgnoreCase(o.getCustomer().getEmail()));
                    return matchName || matchCustomer || matchUser || matchEmail;
                })
                .toList();

        if (myOrders.isEmpty()) {
            myOrders = allOrders;
        }

        return ResponseEntity.ok(myOrders);
    }

    @GetMapping("/api/my-orders/{id}")
    @ResponseBody
    @Operation(summary = "Lấy thông tin chi tiết 1 đơn hàng của tôi theo ID dạng JSON")
    public ResponseEntity<?> getMyOrderDetailApi(@PathVariable("id") Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập!");
        }

        Order order = orderService.findOrderById(id);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy đơn hàng với ID: " + id);
        }

        return ResponseEntity.ok(order);
    }
}