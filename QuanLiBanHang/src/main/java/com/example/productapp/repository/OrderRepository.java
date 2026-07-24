package com.example.productapp.repository;

import com.example.productapp.entity.Order;
import com.example.productapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // HÀM MỚI BỔ SUNG: Tìm đơn hàng theo User
    List<Order> findByUserOrderByOrderDateDesc(User user);
}