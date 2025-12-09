package com.nimbleways.springclean.services.handlers;

import com.nimbleways.springclean.entities.Product;
import com.nimbleways.springclean.enums.ProductType;
import com.nimbleways.springclean.repositories.ProductRepository;
import com.nimbleways.springclean.services.implementations.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Handler for SEASONAL products
 * Manages season-specific availability logic
 */
@Component
@RequiredArgsConstructor
public class SeasonalProductHandler implements ProductHandler {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Override
    public void handle(Product product) {
        if (willDeliveryExceedSeason(product)) {
            markProductAsOutOfStock(product);
        } else if (isBeforeSeasonStart(product)) {
            notifyOutOfStockAndSave(product);
        } else {
            notifyDelay(product);
        }
    }

    @Override
    public boolean supports(ProductType productType) {
        return ProductType.SEASONAL == productType;
    }

    private boolean willDeliveryExceedSeason(Product product) {
        return LocalDate.now()
                .plusDays(product.getLeadTime())
                .isAfter(product.getSeasonEndDate());
    }

    private boolean isBeforeSeasonStart(Product product) {
        return product.getSeasonStartDate().isAfter(LocalDate.now());
    }

    private void markProductAsOutOfStock(Product product) {
        notificationService.sendOutOfStockNotification(product.getName());
        product.setAvailable(0);
        productRepository.save(product);
    }

    private void notifyOutOfStockAndSave(Product product) {
        notificationService.sendOutOfStockNotification(product.getName());
        productRepository.save(product);
    }

    private void notifyDelay(Product product) {
        product.setLeadTime(product.getLeadTime());
        productRepository.save(product);
        notificationService.sendDelayNotification(product.getLeadTime(), product.getName());
    }
}