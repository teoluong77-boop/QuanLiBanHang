package com.example.productapp.controller;

import com.example.productapp.entity.Product;
import com.example.productapp.repository.CategoryRepository;
import com.example.productapp.service.OrderService;
import com.example.productapp.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final OrderService orderService; // 🌟 TIÊM ORDER SERVICE ĐỂ ĐỒNG BỘ DOANH THU CỦA ADMIN

    public ProductController(ProductService productService,
                             CategoryRepository categoryRepository,
                             OrderService orderService) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.orderService = orderService;
    }

    /** Danh sách sản phẩm, tìm kiếm & lọc theo danh mục */
    @GetMapping
    public String listProducts(@RequestParam(name = "keyword", required = false) String keyword,
                               @RequestParam(name = "categoryId", required = false) Long categoryId,
                               Model model) {
        List<Product> products = productService.search(keyword);

        if (categoryId != null) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                    .toList();
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        return "products";
    }

    /** 🌟 FORM THÊM SẢN PHẨM: ĐỒNG BỘ CÙNG CON SỐ TỔNG DOANH THU */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());

        // Lấy con số Doanh thu từ OrderService để hiển thị thành Số dư Ví Admin (Đồng bộ 100%)
        BigDecimal adminBalance = orderService.getTotalRevenue();
        model.addAttribute("adminBalance", adminBalance);

        return "add-product";
    }

    /** Xử lý submit form thêm sản phẩm */
    @PostMapping("/add")
    public String addProduct(@Valid @ModelAttribute("product") Product product,
                             BindingResult bindingResult,
                             @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                             HttpServletRequest request,
                             Model model) throws IOException {

        BigDecimal adminBalance = orderService.getTotalRevenue();

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("adminBalance", adminBalance);
            return "add-product";
        }

        // Xử lý lưu file ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            String uploadDir = "src/main/resources/static/images/";

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = imageFile.getInputStream()) {
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                product.setImageUrl("/images/" + fileName);
            }
        }

        // 🌟 BẮT NGOẠI LỆ NẾU TỔNG DOANH THU/VÍ KHÔNG ĐỦ TIỀN NHẬP HÀNG
        try {
            productService.add(product);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("adminBalance", adminBalance);
            return "add-product";
        }

        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/admin")) {
            return "redirect:/admin";
        }

        return "redirect:/products";
    }

    /** Hiển thị Form Sửa sản phẩm */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.findById(id);
        if (product == null) {
            return "redirect:/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        return "edit-product";
    }

    /** Xử lý Submit Form Sửa sản phẩm */
    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("product") Product product,
                                BindingResult bindingResult,
                                @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                                Model model) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "edit-product";
        }

        Product existingProduct = productService.findById(id);
        if (existingProduct != null) {
            existingProduct.setName(product.getName());
            existingProduct.setImportPrice(product.getImportPrice()); // Cập nhật Giá nhập
            existingProduct.setPrice(product.getPrice());
            existingProduct.setQuantity(product.getQuantity());
            existingProduct.setCategory(product.getCategory());

            existingProduct.setRating(product.getRating());
            existingProduct.setTag(product.getTag());

            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                String uploadDir = "src/main/resources/static/images/";

                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                try (InputStream inputStream = imageFile.getInputStream()) {
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                    existingProduct.setImageUrl("/images/" + fileName);
                }
            }

            productService.add(existingProduct);
        }

        return "redirect:/products";
    }

    /** Chi tiết sản phẩm */
    @GetMapping("/{id}")
    public String productDetail(@PathVariable("id") Long id, Model model) {
        Product product = productService.findById(id);
        if (product == null) {
            return "redirect:/products";
        }
        model.addAttribute("product", product);
        return "product-detail";
    }

    /** Xóa sản phẩm */
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id) {
        productService.deleteById(id);
        return "redirect:/products";
    }
}