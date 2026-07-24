package com.example.productapp.controller;

import com.example.productapp.entity.Order;
import com.example.productapp.service.CartService;
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

    public CheckoutController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    /** Hiển thị trang nhập thông tin đặt hàng */
    @GetMapping
    public String showCheckoutPage(Model model) {
        if (cartService.getItems().isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cartItems", cartService.getItems());
        model.addAttribute("totalAmount", cartService.getTotalAmount());
        return "checkout";
    }

    /** Xử lý bấm nút Đặt hàng */
    @PostMapping
    public String processCheckout(@RequestParam("customerName") String customerName,
                                  @RequestParam("phoneNumber") String phoneNumber,
                                  @RequestParam("address") String address,
                                  @RequestParam(value = "note", required = false) String note,
                                  Authentication authentication,
                                  Model model) {

        String username = (authentication != null) ? authentication.getName() : null;
        Order order = orderService.createOrder(customerName, phoneNumber, address, note, username);
        if (order == null) {
            return "redirect:/cart";
        }

        model.addAttribute("order", order);
        return "order-success";
    }
}