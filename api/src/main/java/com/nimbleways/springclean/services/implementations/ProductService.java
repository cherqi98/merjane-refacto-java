package com.nimbleways.springclean.services.implementations;

import java.util.List;

import com.nimbleways.springclean.enums.ProductType;
import com.nimbleways.springclean.services.handlers.ProductHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.nimbleways.springclean.entities.Product;
import com.nimbleways.springclean.repositories.ProductRepository;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final List<ProductHandler> productHandlers;

    /**
     * Updates product lead time and sends delay notification
     * @param leadTime New lead time in days
     * @param product Product to update
     */
    public void notifyDelay(int leadTime, Product product) {
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }

    /**
     * Handles seasonal product logic based on season dates and lead time
     * @param product Seasonal product to handle
     */
    public void handleSeasonalProduct(Product product) {
        getHandlerForType(ProductType.SEASONAL).handle(product);
    }

    /**
     * Handles expirable product logic based on expiry date and availability
     * @param product Expirable product to handle
     */
    public void handleExpiredProduct(Product product) {
        getHandlerForType(ProductType.EXPIRABLE).handle(product);

    }

    /**
     * Get the appropriate handler for a product type
     * @param productType Type of product
     * @return Handler for the product type
     */
    private ProductHandler getHandlerForType(ProductType productType) {
        return productHandlers.stream()
                .filter(handler -> handler.supports(productType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No handler found for product type: " + productType));
    }

}