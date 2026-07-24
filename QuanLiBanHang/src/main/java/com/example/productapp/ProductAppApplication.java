package com.example.productapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Diem khoi dong cua ung dung.
 *
 * {@code @SpringBootApplication} = @Configuration + @ComponentScan + @EnableAutoConfiguration:
 *   - Configuration: class nay la nguon dinh nghia bean/cau hinh.
 *   - ComponentScan: quet moi @Controller/@Service... trong com.example.productapp.*
 *   - EnableAutoConfiguration: Spring Boot tu cau hinh Web MVC, Thymeleaf... theo classpath.
 */
@SpringBootApplication
public class ProductAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductAppApplication.class, args);
    }
}
