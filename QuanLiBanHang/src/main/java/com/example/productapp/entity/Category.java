package com.example.productapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Một danh mục chứa nhiều sản phẩm
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @Builder.Default // Tránh việc Builder gán products = null
    private List<Product> products = new ArrayList<>();
}