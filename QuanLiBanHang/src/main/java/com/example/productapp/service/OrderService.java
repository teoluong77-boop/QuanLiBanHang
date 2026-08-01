package com.example.productapp.service;

import com.example.productapp.entity.*;
import com.example.productapp.repository.CustomerRepository;
import com.example.productapp.repository.OrderRepository;
import com.example.productapp.repository.ProductRepository;
import com.example.productapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final CustomerService customerService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        CustomerRepository customerRepository,
                        UserRepository userRepository,
                        CartService cartService,
                        CustomerService customerService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.customerService = customerService;
    }

    private BigDecimal totalDeductedImportCost = BigDecimal.ZERO;

    public void deductRevenue(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.totalDeductedImportCost = this.totalDeductedImportCost.add(amount);
        }
    }

    @Transactional
    public Order createOrder(String customerName, String phoneNumber, String address, String note, String email, String paymentMethod, String username) {
        List<CartItem> cartItems = cartService.getItems();

        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng của bạn đang rỗng!");
        }

        User loggedInUser = null;
        Customer customer = null;

        if (username != null && !username.isBlank()) {
            loggedInUser = userRepository.findByUsername(username).orElse(null);
            customer = customerService.getCustomerByUsername(username);
        }

        if (customer == null) {
            customer = Customer.builder()
                    .name(customerName)
                    .phoneNumber(phoneNumber)
                    .address(address)
                    .email(email)
                    .build();
            customer = customerRepository.save(customer);
        } else {
            customer.setName(customerName);
            customer.setAddress(address);
            if (phoneNumber != null && !phoneNumber.isBlank()) {
                customer.setPhoneNumber(phoneNumber);
            }
            customer = customerRepository.save(customer);
        }

        BigDecimal totalAmount = cartService.getTotalAmount();
        String formattedPaymentMethod = (paymentMethod != null && !paymentMethod.isBlank()) ? paymentMethod.toUpperCase().trim() : "COD";
        String orderStatus = "PENDING";

        if ("WALLET".equals(formattedPaymentMethod)) {
            if (loggedInUser == null) {
                throw new RuntimeException("Bạn phải đăng nhập tài khoản để sử dụng phương thức thanh toán bằng Ví tiền!");
            }

            BigDecimal currentBalance = (customer.getBalance() != null) ? customer.getBalance()
                    : (loggedInUser.getBalance() != null ? loggedInUser.getBalance() : BigDecimal.ZERO);

            if (currentBalance.compareTo(totalAmount) < 0) {
                throw new RuntimeException("Ví tiền không đủ! Bạn cần " + totalAmount + " ₫ nhưng ví chỉ có " + currentBalance + " ₫.");
            }

            BigDecimal newBalance = currentBalance.subtract(totalAmount);
            customer.setBalance(newBalance);
            customerRepository.save(customer);

            loggedInUser.setBalance(newBalance);
            userRepository.save(loggedInUser);

            User adminUser = userRepository.findAll().stream()
                    .filter(u -> "ROLE_ADMIN".equalsIgnoreCase(u.getRole()) || "ADMIN".equalsIgnoreCase(u.getRole()))
                    .findFirst().orElse(null);

            if (adminUser != null && !adminUser.getId().equals(loggedInUser.getId())) {
                BigDecimal adminBalance = adminUser.getBalance() != null ? adminUser.getBalance() : BigDecimal.ZERO;
                adminUser.setBalance(adminBalance.add(totalAmount));
                userRepository.save(adminUser);
            }

            orderStatus = "COMPLETED";
        }

        Order order = Order.builder()
                .customerName(customerName)
                .phoneNumber(phoneNumber)
                .address(address)
                .note(note)
                .paymentMethod(formattedPaymentMethod)
                .status(orderStatus)
                .deleted(false)
                .totalAmount(totalAmount)
                .orderDate(LocalDateTime.now())
                .customer(customer)
                .orderDetails(new ArrayList<>())
                .build();

        for (CartItem item : cartItems) {
            Long productId = item.getProduct().getId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm ID " + productId + " không tồn tại!"));

            if (product.getQuantity() != null) {
                int newQuantity = product.getQuantity() - item.getQuantity();
                if (newQuantity < 0) {
                    throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ số lượng trong kho!");
                }
                product.setQuantity(newQuantity);
                productRepository.save(product);
            }

            BigDecimal price = product.getPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(price)
                    .subtotal(subtotal)
                    .build();

            order.getOrderDetails().add(detail);
        }

        Order savedOrder = orderRepository.save(order);
        cartService.clear();
        return savedOrder;
    }

    @Transactional
    public void confirmOrderDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null && "PENDING".equalsIgnoreCase(order.getStatus())) {
            order.setStatus("COMPLETED");

            if ("COD".equalsIgnoreCase(order.getPaymentMethod())) {
                User adminUser = userRepository.findAll().stream()
                        .filter(u -> "ROLE_ADMIN".equalsIgnoreCase(u.getRole()) || "ADMIN".equalsIgnoreCase(u.getRole()))
                        .findFirst().orElse(null);

                if (adminUser != null) {
                    BigDecimal adminBalance = adminUser.getBalance() != null ? adminUser.getBalance() : BigDecimal.ZERO;
                    adminUser.setBalance(adminBalance.add(order.getTotalAmount()));
                    userRepository.save(adminUser);
                }
            }
            orderRepository.save(order);
        }
    }

    @Transactional
    public void deleteOrderById(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setDeleted(true);
            orderRepository.save(order);
        }
    }

    public List<Order> findAllOrders() {
        return orderRepository.findByDeletedFalseOrDeletedIsNullOrderByOrderDateDesc();
    }

    public List<Order> findOrdersByCustomerId(Long customerId) {
        return orderRepository.findOrdersByCustomerIdCustom(customerId);
    }

    // 🌟 THÊM HÀM NÀY ĐỂ LẤY ĐƠN THEO USERNAME (TRÁNH BỊ LỆCH CUSTOMER ID)
    public List<Order> findOrdersByUsername(String username) {
        return orderRepository.findOrdersByUsernameCustom(username);
    }

    public Order findOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal totalCompletedOrders = orderRepository.findAll().stream()
                .filter(order -> "COMPLETED".equalsIgnoreCase(order.getStatus()))
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netRevenue = totalCompletedOrders.subtract(this.totalDeductedImportCost);
        return netRevenue.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : netRevenue;
    }
}