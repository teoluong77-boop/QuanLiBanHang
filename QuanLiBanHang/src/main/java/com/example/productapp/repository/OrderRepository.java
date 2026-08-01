package com.example.productapp.repository;

import com.example.productapp.entity.Customer;
import com.example.productapp.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 🌟 LẤY TẤT CẢ ĐƠN HÀNG DÀNH CHO ADMIN (SẮP XẾP MỚI NHẤT LÊN ĐẦU)
    List<Order> findByDeletedFalseOrDeletedIsNullOrderByOrderDateDesc();

    // 🌟 1. FIX LỖI BÁO THIẾU HÀM Ở DÒNG 199 (findOrdersByCustomerIdCustom)
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND (o.deleted = false OR o.deleted IS NULL) ORDER BY o.orderDate DESC")
    List<Order> findOrdersByCustomerIdCustom(@Param("customerId") Long customerId);

    // 🌟 2. HÀM TRUY VẤN ĐƠN HÀNG THEO USERNAME (HÀM BỒ ĐANG GỌI Ở DÒNG 204)
    @Query("SELECT o FROM Order o WHERE (o.customer.user.username = :username OR o.customerName = :username) AND (o.deleted = false OR o.deleted IS NULL) ORDER BY o.orderDate DESC")
    List<Order> findOrdersByUsernameCustom(@Param("username") String username);

    // Tìm danh sách đơn hàng theo Customer ID
    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);

    // Tìm danh sách đơn hàng theo đối tượng Customer
    List<Order> findByCustomerOrderByOrderDateDesc(Customer customer);

    // Tìm danh sách đơn hàng theo số điện thoại
    List<Order> findByPhoneNumberOrderByOrderDateDesc(String phoneNumber);
}