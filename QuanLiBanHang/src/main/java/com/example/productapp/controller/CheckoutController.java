package com.example.productapp.controller;

import com.example.productapp.entity.Customer;
import com.example.productapp.entity.Order;
import com.example.productapp.service.CartService;
import com.example.productapp.service.CustomerService;
import com.example.productapp.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/checkout")
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

    /** Hiển thị trang nhập thông tin đặt hàng */
    @GetMapping
    public String showCheckoutPage(Authentication authentication, Model model) {
        if (cartService.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cartItems", cartService.getItems());
        model.addAttribute("totalAmount", cartService.getTotalAmount());

        // Lấy thông tin khách hàng nếu đã đăng nhập để tự điền form & hiển thị số dư ví
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
                                  Model model) {

        String username = (authentication != null) ? authentication.getName() : null;
        String email = username != null ? username + "@gmail.com" : "";

        try {
            // Gọi OrderService đã được cập nhật logic trừ tiền ví & cộng tiền admin
            Order order = orderService.createOrder(customerName, phoneNumber, address, note, email, paymentMethod, username);

            if (order == null) {
                return "redirect:/cart";
            }

            model.addAttribute("order", order);
            return "order-success";

        } catch (RuntimeException e) {
            // 🌟 BẮT LỖI TÀI KHOẢN KHÔNG ĐỦ TIỀN HOẶC HẾT HÀNG KHO
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("cartItems", cartService.getItems());
            model.addAttribute("totalAmount", cartService.getTotalAmount());

            if (authentication != null) {
                model.addAttribute("customer", customerService.getCustomerByUsername(authentication.getName()));
            }

            return "checkout"; // Trả lại trang checkout kèm câu thông báo lỗi màu đỏ
        }
    }
}