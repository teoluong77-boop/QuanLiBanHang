package com.example.productapp.service;

import com.example.productapp.entity.Customer;
import com.example.productapp.entity.User;
import com.example.productapp.repository.CustomerRepository;
import com.example.productapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    /** Lấy Customer theo username, nếu chưa có profile thì TỰ ĐỘNG TẠO MỚI liên kết với User */
    @Transactional
    public Customer getCustomerByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return null;
        }

        // Tìm Customer liên kết với User này
        return customerRepository.findByUser(user).orElseGet(() -> {
            // 🌟 NẾU CHƯA CÓ CUSTOMER PROFILES -> TỰ ĐỘNG TẠO MỚI ĐỂ LƯU SỐ DƯ
            Customer newCustomer = Customer.builder()
                    .name(user.getUsername())
                    .email(user.getUsername() + "@gmail.com")
                    .user(user)
                    .balance(BigDecimal.ZERO)
                    .build();
            return customerRepository.save(newCustomer);
        });
    }

    /** Xử lý Nạp tiền vào tài khoản Customer */
    @Transactional
    public void deposit(Long customerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0!");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng ID: " + customerId));

        BigDecimal currentBalance = customer.getBalance() != null ? customer.getBalance() : BigDecimal.ZERO;

        // 🌟 Cộng tiền và lưu lại vào database
        customer.setBalance(currentBalance.add(amount));
        customerRepository.save(customer);
    }
}