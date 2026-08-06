package com.example.productapp.controller;

import com.example.productapp.entity.Product;
import com.example.productapp.service.CategoryService;
import com.example.productapp.service.OrderService;
import com.example.productapp.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@Tag(name = "Admin Controller", description = "Quản lý trang Dashboard Admin và API báo cáo Thống kê")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderService orderService;

    // =========================================================================
    // 🌐 1. TRẢ VỀ GIAO DIỆN WEB THYMELEAF (Dành cho trình duyệt lướt Web)
    // =========================================================================
    @GetMapping
    public String adminDashboard(Model model) {
        List<Product> products = productService.findAll();

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("newProduct", new Product());

        model.addAttribute("productCount", products.size());
        model.addAttribute("orderCount", orderService.findAllOrders().size());

        // 🌟 LẤY DOANH THU THẬT BẰNG BIGDECIMAL
        BigDecimal totalRevenue = orderService.getTotalRevenue();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

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

    // =========================================================================
    // 🚀 2. TRẢ VỀ JSON CHO SWAGGER (Có @ResponseBody để Swagger hiển thị)
    // =========================================================================

    @GetMapping("/api/stats")
    @ResponseBody
    @Operation(summary = "Lấy dữ liệu thống kê tổng quan (Tổng SP, Đơn hàng, Doanh thu)")
    public Map<String, Object> getDashboardStatsApi() {
        Map<String, Object> stats = new HashMap<>();
        List<Product> products = productService.findAll();

        stats.put("productCount", products.size());
        stats.put("orderCount", orderService.findAllOrders().size());

        BigDecimal totalRevenue = orderService.getTotalRevenue();
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        return stats;
    }

    @GetMapping("/api/chart-data")
    @ResponseBody
    @Operation(summary = "Lấy danh sách Tên & Số lượng tồn kho phục vụ vẽ Biểu đồ")
    public Map<String, Object> getChartDataApi() {
        Map<String, Object> chartData = new HashMap<>();
        List<Product> products = productService.findAll();

        List<String> chartNames = products.stream().map(Product::getName).toList();
        List<Integer> chartQuantities = products.stream().map(p -> p.getQuantity() != null ? p.getQuantity() : 0).toList();

        chartData.put("chartNames", chartNames);
        chartData.put("chartQuantities", chartQuantities);

        return chartData;
    }
}