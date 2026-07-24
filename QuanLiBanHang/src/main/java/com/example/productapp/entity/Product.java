package com.example.productapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @Min(value = 1, message = "Giá phải lớn hơn 0")
    @Column(nullable = false)
    private Double price;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer quantity;

    private String description;

    // Trường lưu đường dẫn ảnh sản phẩm (Ví dụ: /images/17100000_sanpham.jpg)
    private String imageUrl;

    // 🌟 BỔ SUNG 2 TRƯỜNG ĐÁNH GIÁ VÀ NHÃN SẢN PHẨM
    private String rating; // Lưu sao/đánh giá (Ví dụ: 4.8 / 5 ⭐)
    private String tag;    // Lưu nhãn (Ví dụ: Bán chạy, Nổi bật, Hàng ế...)

    // Mối quan hệ N-1 với bảng Category (Một danh mục có nhiều sản phẩm)
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}