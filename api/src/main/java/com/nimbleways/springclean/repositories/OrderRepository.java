package com.nimbleways.springclean.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nimbleways.springclean.entities.Order;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findById(Long orderId);
}
