# Lab 3 — Spring MVC: Ứng dụng quản lý sản phẩm

Ứng dụng web quản lý sản phẩm đơn giản xây dựng bằng **Spring Boot + Spring MVC + Thymeleaf**,
theo đề bài `Ch3_BT_Spring_MVC_in_class.pdf`.

Công nghệ: JDK 21 · Maven · Spring Boot 3.2.3 · Spring MVC · Thymeleaf · Bean Validation · DevTools.

## Chức năng

**Cơ bản (mục 1–9 của đề):**
1. Xem danh sách sản phẩm — `GET /products`
2. Thêm sản phẩm mới (có kiểm tra dữ liệu) — `GET /products/add`, `POST /products/add`
3. Xem chi tiết sản phẩm theo ID — `GET /products/{id}`

**Bài tập mở rộng (mục 11 của đề):**
1. **Xóa sản phẩm** — `POST /products/delete/{id}` (nút "Xóa" ở mỗi dòng)
2. **Tìm kiếm theo tên** — `GET /products?keyword=...` (không phân biệt hoa/thường)
3. **Format giá tiền theo VNĐ** — hiển thị `15.000.000 ₫` (dùng `#numbers` của Thymeleaf trong view,
   và phương thức `ProductService.formatPrice(...)` phía Java)
4. **Tách xử lý dữ liệu sang `ProductService`** — Controller không còn giữ danh sách, chỉ gọi Service

## Cấu trúc

```
lab-3/
├── pom.xml                                  # web + thymeleaf + validation + devtools, Java 21
└── src/
    ├── main/
    │   ├── java/com/example/productapp/
    │   │   ├── ProductAppApplication.java       # @SpringBootApplication + main()
    │   │   ├── controller/
    │   │   │   ├── ProductController.java        # list / add / detail / search / delete
    │   │   │   └── HomeController.java            # "/" -> redirect "/products"
    │   │   ├── service/ProductService.java       # lưu trữ in-memory + nghiệp vụ
    │   │   └── model/Product.java                # @NotBlank, @Min validation
    │   └── resources/
    │       ├── application.properties
    │       ├── static/css/style.css
    │       └── templates/
    │           ├── products.html                 # danh sách + tìm kiếm + xóa
    │           ├── add-product.html              # form thêm + báo lỗi validation
    │           └── product-detail.html
    └── test/java/com/example/productapp/
        ├── service/ProductServiceTest.java       # unit test nghiệp vụ
        └── controller/ProductControllerTest.java # MockMvc test tầng web
```

## Cách chạy

**IntelliJ IDEA:** mở thư mục `lab-3`, đợi Maven import xong, chạy `ProductAppApplication.java`.

**Dòng lệnh:**
```bash
mvn spring-boot:run
```
Hoặc đóng gói rồi chạy jar:
```bash
mvn clean package
java -jar target/productapp-0.0.1-SNAPSHOT.jar
```

Sau đó truy cập: **http://localhost:8080/products** (hoặc http://localhost:8080/ sẽ tự chuyển hướng).

## Chạy test

```bash
mvn test
```

## Ghi chú kỹ thuật

- **`@SpringBootApplication`** = `@Configuration` + `@ComponentScan` + `@EnableAutoConfiguration`.
- **Validation:** `@Valid @ModelAttribute Product` + `BindingResult`. Nếu `bindingResult.hasErrors()`
  thì trả lại form `add-product` và hiển thị lỗi qua `th:errors`; nếu hợp lệ thì lưu và `redirect:/products`.
- **Sinh ID:** dùng bộ đếm tăng dần (`AtomicInteger`) nên ID không trùng kể cả sau khi xóa
  (không dùng `size()+1` vì sẽ trùng sau khi xóa).
- **Định tuyến:** `GET /products/add` (đường dẫn tường minh) được ưu tiên hơn mẫu `GET /products/{id}`,
  nên hai route không xung đột.
