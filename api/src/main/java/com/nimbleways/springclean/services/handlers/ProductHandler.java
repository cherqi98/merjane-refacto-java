package com.nimbleways.springclean.services.handlers;

import com.nimbleways.springclean.entities.Product;
import com.nimbleways.springclean.enums.ProductType;

/**
 * Strategy interface for handling different product types
 * Following Open/Closed Principle: open for extension, closed for modification
 */
public interface ProductHandler {

    /**
     * Handle product-specific logic when out of stock or special conditions
     * @param product Product to handle
     */
    void handle(Product product);

    /**
     * Check if this handler supports the given product type
     * @param productType Type of product
     * @return true if this handler supports the product type
     */
    boolean supports(ProductType productType);
}