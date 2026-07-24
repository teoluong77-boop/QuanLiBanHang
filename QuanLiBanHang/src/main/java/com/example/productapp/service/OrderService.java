package com.example.productapp.service;

import com.example.productapp.entity.*;
import com.example.productapp.repository.OrderRepository;
import com.example.productapp.repository.ProductRepository;
import com.example.productapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    @Transactional
    public Order createOrder(String customerName, String phoneNumber, String address, String note, String username) {
        List<CartItem> cartItems = cartService.getItems();
        if (cartItems.isEmpty()) {
            return null;
        }

        User user = null;
        if (username != null && !username.isBlank()) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        Order order = Order.builder()
                .customerName(customerName)
                .phoneNumber(phoneNumber)
                .address(address)
                .note(note)
                .totalAmount(cartService.getTotalAmount())
                .orderDate(LocalDateTime.now())
                .user(user)
                .build();

        for (CartItem item : cartItems) {
            Product product = item.getProduct();

            // Kiem tra va Tru ton kho
            if (product.getQuantity() != null) {
                int newQuantity = product.getQuantity() - item.getQuantity();
                if (newQuantity < 0) {
                    throw new RuntimeException("San pham " + product.getName() + " khong du so luong trong kho!");
                }
                product.setQuantity(newQuantity);
                productRepository.save(product);
            }

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(product.getPrice())
                    .build();
            order.getOrderDetails().add(detail);
        }

        Order savedOrder = orderRepository.save(order);
        cartService.clear();
        return savedOrder;
    }

    // Lay tat ca don hang
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    // Lay danh sach don hang theo username
    public List<Order> findOrdersByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return List.of();
        }
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    // Lay chi tiet 1 don hang
    public Order findOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    // Tinh tong doanh thu
    public double getTotalRevenue() {
        return orderRepository.findAll().stream().mapToDouble(Order::getTotalAmount).sum();
    }
}