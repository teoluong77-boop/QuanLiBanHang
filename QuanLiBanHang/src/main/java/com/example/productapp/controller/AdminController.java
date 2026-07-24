package com.example.productapp.controller;

import com.example.productapp.entity.Product;
import com.example.productapp.service.CategoryService;
import com.example.productapp.service.OrderService;
import com.example.productapp.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String adminDashboard(Model model) {
        List<Product> products = productService.findAll();

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("newProduct", new Product());

        model.addAttribute("productCount", products.size());
        model.addAttribute("orderCount", orderService.findAllOrders().size());

        // 🌟 LẤY DOANH THU THẬT TỪ DATABASE BẰNG HÀM CÓ SẴN CỦA ORDERSERVICE
        model.addAttribute("totalRevenue", (long) orderService.getTotalRevenue());

        // Lấy dữ liệu thật từ Database cho biểu đồ Chart.js
        List<String> chartNames = products.stream()
                .map(Product::getName)
                .toList();

        List<Integer> chartQuantities = products.stream()
                .map(p -> p.getQuantity() != null ? p.getQuantity() : 0)
                .toList();

        model.addAttribute("chartNames", chartNames);
        model.addAttribute("chartQuantities", chartQuantities);

        return "admin-dashboard";
    }
}