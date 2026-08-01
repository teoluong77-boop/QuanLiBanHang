package com.example.productapp.service;

import com.example.productapp.entity.Product;
import com.example.productapp.repository.OrderDetailRepository;
import com.example.productapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderService orderService; // 🌟 TIÊM ORDER SERVICE ĐỂ TRỪ TIỀN DOANH THU/VÍ

    /** Danh sách tất cả sản phẩm từ CSDL MySQL */
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /** Tìm kiếm sản phẩm theo tên */
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String kw = keyword.trim().toLowerCase();
        return productRepository.findAll().stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(kw))
                .toList();
    }

    /** Tìm sản phẩm theo id */
    public Product findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    /** 🌟 THÊM SẢN PHẨM MỚI VÀ TRỪ TIỀN VỐN NHẬP HÀNG TRỰC TIẾP VÀO DOANH THU/VÍ ADMIN */
    @Transactional
    public Product add(Product product) {
        // Chỉ trừ tiền vốn khi TẠO SẢN PHẨM MỚI (Chưa có ID)
        if (product.getId() == null) {
            BigDecimal importPrice = product.getImportPrice() != null ? product.getImportPrice() : BigDecimal.ZERO;
            int quantity = product.getQuantity() != null ? product.getQuantity() : 0;

            // Tính Tổng tiền vốn nhập = Giá nhập x Số lượng
            BigDecimal totalImportCost = importPrice.multiply(BigDecimal.valueOf(quantity));

            if (totalImportCost.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal currentRevenue = orderService.getTotalRevenue();

                // KIỂM TRA SỐ DƯ DOANH THU/VÍ HIỆN CÓ
                if (currentRevenue.compareTo(totalImportCost) < 0) {
                    throw new RuntimeException("Số dư Ví/Doanh thu không đủ để nhập lô hàng này! Cần vốn: "
                            + totalImportCost + " ₫ nhưng ví chỉ có: " + currentRevenue + " ₫");
                }

                // 🌟 GỌI ORDER SERVICE TRỪ TIỀN TRỰC TIẾP -> CON SỐ TRÊN GIAO DIỆN SẼ TỤT XUỐNG NGAY!
                orderService.deductRevenue(totalImportCost);
            }
        }

        return productRepository.save(product);
    }

    /** Xóa sản phẩm theo id */
    @Transactional
    public boolean deleteById(Long id) {
        if (productRepository.existsById(id)) {
            orderDetailRepository.deleteByProductId(id);
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /** Format giá tiền theo VN */
    public String formatPrice(double price) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
        return formatter.format(price) + " ₫";
    }
}