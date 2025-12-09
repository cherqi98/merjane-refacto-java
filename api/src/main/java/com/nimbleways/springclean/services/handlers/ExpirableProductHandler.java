package com.nimbleways.springclean.services.handlers;

import com.nimbleways.springclean.entities.Product;
import com.nimbleways.springclean.enums.ProductType;
import com.nimbleways.springclean.repositories.ProductRepository;
import com.nimbleways.springclean.services.implementations.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Handler for EXPIRABLE products
 * Manages expiry date logic
 */
@Component
@RequiredArgsConstructor
public class ExpirableProductHandler implements ProductHandler {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Override
    public void handle(Product product) {
        if (isAvailableAndNotExpired(product)) {
            decrementProductStock(product);
        } else {
            markProductAsExpired(product);
        }
    }

    @Override
    public boolean supports(ProductType productType) {
        return ProductType.EXPIRABLE == productType;
    }

    private boolean isAvailableAndNotExpired(Product product) {
        return product.getAvailable() > 0
                && product.getExpiryDate().isAfter(LocalDate.now());
    }

    private void decrementProductStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }

    private void markProductAsExpired(Product product) {
        notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
        product.setAvailable(0);
        productRepository.save(product);
    }
}