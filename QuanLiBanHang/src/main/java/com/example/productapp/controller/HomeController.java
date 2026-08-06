package com.example.productapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Chuyển hướng trang gốc "/" sang danh sách sản phẩm cho tiện truy cập.
 */
@Controller
@Tag(name = "Home Controller", description = "Điều hướng trang chủ và kiểm tra trạng thái ứng dụng")
public class HomeController {

    // =========================================================================
    // 🌐 1. TRẢ VỀ GIAO DIỆN WEB THYMELEAF (Dành cho trình duyệt lướt Web)
    // =========================================================================

    @GetMapping("/")
    public String home() {
        return "redirect:/products";
    }

    // =========================================================================
    // 🚀 2. TRẢ VỀ JSON CHO SWAGGER (Có @ResponseBody để Swagger quét được)
    // =========================================================================

    @GetMapping("/api/status")
    @ResponseBody
    @Operation(summary = "Kiểm tra trạng thái hệ thống (Health check / Ping API)")
    public ResponseEntity<Map<String, String>> getSystemStatusApi() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "QuanLiBanHang API đang hoạt động bình thường",
                "redirectUrl", "/products"
        ));
    }
}