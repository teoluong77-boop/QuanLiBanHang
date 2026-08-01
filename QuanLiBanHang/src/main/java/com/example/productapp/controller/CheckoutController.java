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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        // 🌟 1. GIỎ HÀNG RỖNG THÌ CHUYỂN BẮT BỘC VỀ TRANG GIỎ HÀNG
        if (cartService.getItems() == null || cartService.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cartItems", cartService.getItems());
        model.addAttribute("totalAmount", cartService.getTotalAmount());

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
                                  RedirectAttributes redirectAttributes) {

        // 🌟 2. GIỎ HÀNG RỖNG THÌ KHÔNG CHO PHÉP XỬ LÝ
        if (cartService.getItems() == null || cartService.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        String username = (authentication != null) ? authentication.getName() : null;
        String email = username != null ? username + "@gmail.com" : "";

        try {
            Order order = orderService.createOrder(customerName, phoneNumber, address, note, email, paymentMethod, username);

            if (order == null) {
                return "redirect:/cart";
            }

            // 🌟 3. ĐẶT HÀNG THÀNH CÔNG -> REDIRECT SANG TRANG THÀNH CÔNG (CHỐNG REFRESH DỰ LIỆU)
            redirectAttributes.addFlashAttribute("order", order);
            return "redirect:/checkout/success";

        } catch (Exception e) {
            // 🌟 4. BẮT LỖI VÀ REDIRECT VỀ /checkout
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Có lỗi xảy ra trong quá trình xử lý đơn hàng!");
            return "redirect:/checkout";
        }
    }

    /** 🌟 5. TRANG HIỂN THỊ ĐẶT HÀNG THÀNH CÔNG */
    @GetMapping("/success")
    public String orderSuccess() {
        return "order-success";
    }
}