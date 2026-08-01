package com.example.productapp.repository;

import com.example.productapp.entity.Customer;
import com.example.productapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    // 🌟 THÊM HÀM NÀY ĐỂ LẤY THÔNG TIN KHÁCH HÀNG TỪ USER ĐĂNG NHẬP
    Optional<Customer> findByUser(User user);
}