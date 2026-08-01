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
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        CustomerRepository customerRepository,
                        UserRepository userRepository,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    @Transactional
    public Order createOrder(String customerName, String phoneNumber, String address, String note, String email, String paymentMethod, String username) {
        List<CartItem> cartItems = cartService.getItems();
        if (cartItems.isEmpty()) {
            return null;
        }

        // 1. TẠO HOẶC TÌM KHÁCH HÀNG (CUSTOMER)
        Customer customer = null;
        if (username != null && !username.isBlank()) {
            User loggedInUser = userRepository.findByUsername(username).orElse(null);
            if (loggedInUser != null) {
                customer = customerRepository.findByUser(loggedInUser).orElse(null);
            }
        }

        if (customer == null) {
            customer = customerRepository.findByPhoneNumber(phoneNumber)
                    .orElseGet(() -> Customer.builder()
                            .name(customerName)
                            .phoneNumber(phoneNumber)
                            .address(address)
                            .email(email)
                            .build());
        }

        customer = customerRepository.save(customer);

        // 2. KHỞI TẠO ĐƠN HÀNG VỚI TỔNG TIỀN BIGDECIMAL
        BigDecimal totalAmount = cartService.getTotalAmount();

        // 🌟 BƯỚC 2 & 3: XỬ LÝ THANH TOÁN BẰNG VÍ TIỀN (WALLET)
        if ("WALLET".equalsIgnoreCase(paymentMethod)) {
            if (username == null || username.isBlank()) {
                throw new RuntimeException("Bạn phải đăng nhập tài khoản để sử dụng phương thức thanh toán bằng Ví tiền!");
            }

            BigDecimal currentBalance = customer.getBalance() != null ? customer.getBalance() : BigDecimal.ZERO;

            // KIỂM TRA SỐ DƯ
            if (currentBalance.compareTo(totalAmount) < 0) {
                throw new RuntimeException("Tài khoản của bạn không đủ tiền! Vui lòng nạp thêm tiền vào ví hoặc chọn phương thức COD.");
            }

            // TRỪ TIỀN KHÁCH HÀNG
            customer.setBalance(currentBalance.subtract(totalAmount));
            customerRepository.save(customer);

            // CỘNG TIỀN CHO TÀI KHOẢN ADMIN / CHỦ SHOP
            User adminUser = userRepository.findAll().stream()
                    .filter(u -> "ROLE_ADMIN".equalsIgnoreCase(u.getRole()) || "ADMIN".equalsIgnoreCase(u.getRole()))
                    .findFirst().orElse(null);

            if (adminUser != null) {
                BigDecimal adminBalance = adminUser.getBalance() != null ? adminUser.getBalance() : BigDecimal.ZERO;
                adminUser.setBalance(adminBalance.add(totalAmount));
                userRepository.save(adminUser);
            }
        }

        Order order = Order.builder()
                .customerName(customerName)
                .phoneNumber(phoneNumber)
                .address(address)
                .note(note)
                .paymentMethod(paymentMethod != null ? paymentMethod : "COD")
                .totalAmount(totalAmount)
                .orderDate(LocalDateTime.now())
                .customer(customer)
                .build();

        // 3. XỬ LÝ TỪNG CHI TIẾT ĐƠN HÀNG (ORDER DETAIL)
        for (CartItem item : cartItems) {
            Product product = item.getProduct();

            // Kiểm tra và trừ tồn kho
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

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> findOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    public Order findOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public BigDecimal getTotalRevenue() {
        return orderRepository.findAll().stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}