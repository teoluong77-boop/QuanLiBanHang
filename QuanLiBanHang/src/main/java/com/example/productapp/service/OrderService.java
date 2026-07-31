package com.example.productapp.service;

import com.example.productapp.entity.*;
import com.example.productapp.repository.CustomerRepository;
import com.example.productapp.repository.OrderRepository;
import com.example.productapp.repository.ProductRepository;
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
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        CustomerRepository customerRepository,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.cartService = cartService;
    }

    @Transactional
    public Order createOrder(String customerName, String phoneNumber, String address, String note, String email) {
        List<CartItem> cartItems = cartService.getItems();
        if (cartItems.isEmpty()) {
            return null;
        }

        // 1. TẠO HOẶC TÌM KHÁCH HÀNG (CUSTOMER) THEO SỐ ĐIỆN THOẠI
        Customer customer = customerRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> Customer.builder()
                        .name(customerName)
                        .phoneNumber(phoneNumber)
                        .address(address)
                        .email(email)
                        .build());

        customer = customerRepository.save(customer);

        // 2. KHỞI TẠO ĐƠN HÀNG VỚI TỔNG TIỀN BIGDECIMAL
        BigDecimal totalAmount = cartService.getTotalAmount();

        Order order = Order.builder()
                .customerName(customerName)
                .phoneNumber(phoneNumber)
                .address(address)
                .note(note)
                .totalAmount(totalAmount)
                .orderDate(LocalDateTime.now())
                .customer(customer) // Gắn liên kết với Customer
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

            // Tính đơn giá và thành tiền (subtotal = price * quantity)
            BigDecimal price = product.getPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(price)
                    .subtotal(subtotal) // 🌟 BỔ SUNG SUBTOTAL THEO Ý THẦY
                    .build();

            order.getOrderDetails().add(detail);
        }

        Order savedOrder = orderRepository.save(order);
        cartService.clear();
        return savedOrder;
    }

    // Lấy tất cả đơn hàng
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    // Lấy danh sách đơn hàng theo id của Customer
    public List<Order> findOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    // Lấy chi tiết 1 đơn hàng
    public Order findOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    // 🌟 TÍNH TỔNG DOANH THU DÙNG BIGDECIMAL
    public BigDecimal getTotalRevenue() {
        return orderRepository.findAll().stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}