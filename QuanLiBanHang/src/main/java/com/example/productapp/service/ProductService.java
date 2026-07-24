package com.example.productapp.service;

import com.example.productapp.entity.Product;
import com.example.productapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

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
    public boolean deleteById(Long id) {
        if (productRepository.existsById(id)) {
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