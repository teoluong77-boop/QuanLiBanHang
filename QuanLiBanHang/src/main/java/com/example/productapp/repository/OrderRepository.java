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

    List<Order> findByDeletedFalseOrDeletedIsNullOrderByOrderDateDesc();

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND (o.deleted = false OR o.deleted IS NULL) ORDER BY o.orderDate DESC")
    List<Order> findOrdersByCustomerIdCustom(@Param("customerId") Long customerId);

    // 🌟 TRUY VẤN BAO PHỦ 100%: DÙ DATABASE CÓ BỊ KHUYẾT USER_ID HAY CHƯA LINK DỮ LIỆU CŨNG VỚT ĐƯỢC HẾT ĐƠN
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.customer c LEFT JOIN c.user u " +
            "WHERE (u.username = :username OR c.user.username = :username OR c.id = :customerId OR o.customerName = :username OR c.email = :username) " +
            "AND (o.deleted = false OR o.deleted IS NULL) " +
            "ORDER BY o.orderDate DESC")
    List<Order> findOrdersByUsernameOrCustomerIdCustom(@Param("username") String username, @Param("customerId") Long customerId);

    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);
    List<Order> findByCustomerOrderByOrderDateDesc(Customer customer);
    List<Order> findByPhoneNumberOrderByOrderDateDesc(String phoneNumber);
}