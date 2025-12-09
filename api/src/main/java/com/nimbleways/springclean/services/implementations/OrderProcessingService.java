package com.nimbleways.springclean.services.implementations;

import com.nimbleways.springclean.entities.Product;
import com.nimbleways.springclean.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Service responsible for processing orders
 * Encapsulates the business logic for different product types
 */
@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final ProductRepository productRepository;
    private final ProductService productService;

    /**
     * Process an order item based on its product type
     * @param product Product to process
     */
    public void processOrderItem(Product product) {
        switch (product.getType()) {
            case NORMAL:
                processNormalProduct(product);
                break;
            case SEASONAL:
                processSeasonalProduct(product);
                break;
            case EXPIRABLE:
                processExpirableProduct(product);
                break;
            default:
                throw new IllegalArgumentException("Unknown product type: " + product.getType());
        }
    }

    /**
     * Process a normal product order
     */
    private void processNormalProduct(Product product) {
        if (isProductAvailable(product)) {
            decrementStock(product);
        } else if (hasLeadTime(product)) {
            productService.notifyDelay(product.getLeadTime(), product);
        }
    }

    /**
     * Process a seasonal product order
     */
    private void processSeasonalProduct(Product product) {
        if (isInSeasonAndAvailable(product)) {
            decrementStock(product);
        } else {
            productService.handleSeasonalProduct(product);
        }
    }

    /**
     * Process an expirable product order
     */
    private void processExpirableProduct(Product product) {
        if (isAvailableAndNotExpired(product)) {
            decrementStock(product);
        } else {
            productService.handleExpiredProduct(product);
        }
    }

    // ==================== Helper Methods ====================

    private boolean isProductAvailable(Product product) {
        return product.getAvailable() > 0;
    }

    private boolean hasLeadTime(Product product) {
        return product.getLeadTime() > 0;
    }

    private boolean isInSeasonAndAvailable(Product product) {
        LocalDate now = LocalDate.now();
        return now.isAfter(product.getSeasonStartDate())
                && now.isBefore(product.getSeasonEndDate())
                && product.getAvailable() > 0;
    }

    private boolean isAvailableAndNotExpired(Product product) {
        return product.getAvailable() > 0
                && product.getExpiryDate().isAfter(LocalDate.now());
    }

    private void decrementStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }
}