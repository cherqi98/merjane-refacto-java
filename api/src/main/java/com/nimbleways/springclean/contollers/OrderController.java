package com.nimbleways.springclean.contollers;

import com.nimbleways.springclean.dto.product.ProcessOrderResponse;
import com.nimbleways.springclean.entities.Order;
import com.nimbleways.springclean.repositories.OrderRepository;
import com.nimbleways.springclean.services.implementations.OrderProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Order Controller
 * Follows Single Responsibility Principle - handles only HTTP concerns
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderProcessingService orderProcessingService;
    private final OrderRepository orderRepository;

    @PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable Long orderId) {
        Order order = findOrderById(orderId);
        processAllOrderItems(order);
        return new ProcessOrderResponse(order.getId());
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    private void processAllOrderItems(Order order) {
        order.getItems().forEach(orderProcessingService::processOrderItem);
    }
}