package com.nimbleways.springclean.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nimbleways.springclean.entities.Product;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findById(Long productId);

    Optional<Product> findFirstByName(String name);
}
