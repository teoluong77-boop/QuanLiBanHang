package com.example.productapp.repository;

import com.example.productapp.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    // 🌟 Thêm hàm xóa tất cả chi tiết đơn hàng theo productId
    void deleteByProductId(Long productId);
}