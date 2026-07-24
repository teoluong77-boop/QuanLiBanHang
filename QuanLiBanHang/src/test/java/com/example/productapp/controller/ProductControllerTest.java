package com.example.productapp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Test tang web (Spring MVC) bang MockMvc. {@code @DirtiesContext} tao lai context
 * truoc moi test de trang thai in-memory cua ProductService luon sach.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listProductsShowsSeedData() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attribute("products", hasSize(2)))
                .andExpect(content().string(containsString("Laptop")))
                .andExpect(content().string(containsString("15.000.000 ₫"))); // format VND trong view
    }

    @Test
    void searchFiltersByName() throws Exception {
        mockMvc.perform(get("/products").param("keyword", "lap"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attribute("products", hasSize(1)));
    }

    @Test
    void showAddFormReturnsEmptyProduct() throws Exception {
        mockMvc.perform(get("/products/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-product"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    void addValidProductRedirectsAndPersists() throws Exception {
        mockMvc.perform(post("/products/add")
                        .param("name", "Keyboard")
                        .param("price", "500000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        mockMvc.perform(get("/products"))
                .andExpect(model().attribute("products", hasSize(3)));
    }

    @Test
    void addInvalidProductReturnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/products/add")
                        .param("name", "")      // vi pham @NotBlank
                        .param("price", "0"))   // vi pham @Min(1)
                .andExpect(status().isOk())
                .andExpect(view().name("add-product"))
                .andExpect(model().attributeHasFieldErrors("product", "name", "price"));
    }

    @Test
    void productDetailByIdShowsProduct() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-detail"))
                .andExpect(model().attributeExists("product"))
                .andExpect(content().string(containsString("Laptop")));
    }

    @Test
    void deleteProductRedirectsAndRemoves() throws Exception {
        mockMvc.perform(post("/products/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        mockMvc.perform(get("/products"))
                .andExpect(model().attribute("products", hasSize(1)));
    }
}
