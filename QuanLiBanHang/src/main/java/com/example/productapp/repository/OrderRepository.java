package com.example.productapp.repository;

import com.example.productapp.entity.Customer;
import com.example.productapp.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 🌟 LẤY TẤT CẢ ĐƠN HÀNG CHƯA XÓA (DELETED IS FALSE HOẶC NULL)
    List<Order> findByDeletedFalseOrDeletedIsNull();

    // 🌟 LẤY ĐƠN HÀNG THEO CUSTOMER ID CHƯA XÓA
    List<Order> findByCustomerIdAndDeletedFalseOrCustomerIdAndDeletedIsNullOrderByOrderDateDesc(Long customerId1, Long customerId2);

    // Tìm danh sách đơn hàng theo Customer ID và sắp xếp theo ngày giảm dần
    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);

    // Tìm danh sách đơn hàng theo đối tượng Customer
    List<Order> findByCustomerOrderByOrderDateDesc(Customer customer);

    // Tìm danh sách đơn hàng theo số điện thoại
    List<Order> findByPhoneNumberOrderByOrderDateDesc(String phoneNumber);
}