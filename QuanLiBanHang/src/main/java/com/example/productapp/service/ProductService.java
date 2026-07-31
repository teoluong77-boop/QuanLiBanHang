package com.example.productapp.service;

import com.example.productapp.entity.Product;
import com.example.productapp.repository.OrderDetailRepository;
import com.example.productapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository; // 🌟 Tiêm OrderDetailRepository xử lý khóa ngoại

    /** Danh sach tat ca san pham tu CSDL MySQL */
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /** Tim kiem san pham theo ten (khong phan biet hoa/thuong) */
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String kw = keyword.trim().toLowerCase();
        return productRepository.findAll().stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(kw))
                .toList();
    }

    /** Tim san pham theo id (Long), tra ve null neu khong co */
    public Product findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    /** Them hoac cap nhat san pham vao CSDL */
    public Product add(Product product) {
        return productRepository.save(product);
    }

    /** Xoa san pham theo id */
    @Transactional // 🌟 BẮT BỘC DÙNG @Transactional ĐỂ XÓA LIÊN BẢNG
    public boolean deleteById(Long id) {
        if (productRepository.existsById(id)) {
            // Step 1: Xóa tất cả các chi tiết đơn hàng chứa sản phẩm này trước
            orderDetailRepository.deleteByProductId(id);

            // Step 2: Xóa sản phẩm ra khỏi DB
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /** Format gia tien theo VN, vi du "15.000.000 ₫" */
    public String formatPrice(double price) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
        return formatter.format(price) + " ₫";
    }
}