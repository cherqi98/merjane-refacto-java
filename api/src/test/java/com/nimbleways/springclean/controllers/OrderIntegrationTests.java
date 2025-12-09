package com.nimbleways.springclean.controllers;

import com.nimbleways.springclean.entities.Order;
import com.nimbleways.springclean.entities.Product;
import com.nimbleways.springclean.enums.ProductType;
import com.nimbleways.springclean.repositories.OrderRepository;
import com.nimbleways.springclean.repositories.ProductRepository;
import com.nimbleways.springclean.services.implementations.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.assertEquals;

// import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Specify the controller class you want to test
// This indicates to spring boot to only load UsersController into the context
// Which allows a better performance and needs to do less mocks
@SpringBootTest
@AutoConfigureMockMvc
public class OrderIntegrationTests {
        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private NotificationService notificationService;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private ProductRepository productRepository;

        @Test
        public void processOrderShouldReturn() throws Exception {
                List<Product> allProducts = createProducts();
                Set<Product> orderItems = new HashSet<Product>(allProducts);
                Order order = createOrder(orderItems);
                productRepository.saveAll(allProducts);
                order = orderRepository.save(order);
                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                                .contentType("application/json"))
                                .andExpect(status().isOk());
                Order resultOrder = orderRepository.findById(order.getId()).orElseThrow();
                assertEquals(resultOrder.getId(), order.getId());
        }

        private static Order createOrder(Set<Product> products) {
                Order order = new Order();
                order.setItems(products);
                return order;
        }

    private static List<Product> createProducts() {
        List<Product> products = new ArrayList<>();

        // NORMAL products
        products.add(new Product(
                null, 15, 30, ProductType.NORMAL, "USB Cable", null, null, null
        ));
        products.add(new Product(
                null, 10, 0, ProductType.NORMAL, "USB Dongle", null, null, null
        ));

        // EXPIRABLE products
        products.add(new Product(
                null, 15, 30, ProductType.EXPIRABLE, "Butter",
                LocalDate.now().plusDays(26), null, null
        ));
        products.add(new Product(
                null, 90, 6, ProductType.EXPIRABLE, "Milk",
                LocalDate.now().minusDays(2), null, null  // Expired
        ));

        // SEASONAL products
        products.add(new Product(
                null, 15, 30, ProductType.SEASONAL, "Watermelon", null,
                LocalDate.now().minusDays(2),    // Season started
                LocalDate.now().plusDays(58)     // Season ends in future
        ));
        products.add(new Product(
                null, 15, 30, ProductType.SEASONAL, "Grapes", null,
                LocalDate.now().plusDays(180),   // Season starts in future
                LocalDate.now().plusDays(240)
        ));

        return products;
    }
}
