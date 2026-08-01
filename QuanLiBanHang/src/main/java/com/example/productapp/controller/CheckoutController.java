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

        if (cartService.getItems() == null || cartService.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        String username = (authentication != null) ? authentication.getName() : null;

        // 🌟 Lấy email thật từ Customer nếu có, tránh tạo chuỗi ảo gây lỗi DB
        String email = "";
        if (username != null) {
            Customer existingCustomer = customerService.getCustomerByUsername(username);
            if (existingCustomer != null && existingCustomer.getEmail() != null) {
                email = existingCustomer.getEmail();
            }
        }

        try {
            Order order = orderService.createOrder(customerName, phoneNumber, address, note, email, paymentMethod, username);

            if (order == null) {
                return "redirect:/cart";
            }

            redirectAttributes.addFlashAttribute("order", order);
            return "redirect:/checkout/success";

        } catch (Exception e) {
            // 🌟 IN LỖI RA CONSOLE ĐỂ DỄ BẮT BỆNH NẾU DÍNH ROLLBACK
            System.err.println("❌ LỖI TẠO ĐƠN HÀNG: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Có lỗi xảy ra trong quá trình xử lý đơn hàng!");
            return "redirect:/checkout";
        }
    }

    /** TRANG HIỂN THỊ ĐẶT HÀNG THÀNH CÔNG */
    @GetMapping("/success")
    public String orderSuccess() {
        return "order-success";
    }
}