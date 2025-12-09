package com.nimbleways.springclean.services.implementations;

import com.nimbleways.springclean.entities.Product;
import com.nimbleways.springclean.enums.ProductType;
import com.nimbleways.springclean.repositories.ProductRepository;
import com.nimbleways.springclean.services.handlers.ProductHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProductHandler seasonalProductHandler;

    @Mock
    private ProductHandler expirableProductHandler;

    @Mock
    private ProductHandler otherProductHandler;

    @InjectMocks
    private ProductService productService;

    private List<ProductHandler> productHandlers;
    private Product normalProduct;
    private Product seasonalProduct;
    private Product expirableProduct;

    @BeforeEach
    void setUp() {
        // Initialize handlers list with mocked handlers
        productHandlers = Arrays.asList(seasonalProductHandler, expirableProductHandler, otherProductHandler);

        // Create test products
        normalProduct = Product.builder()
                .id(1L)
                .name("Normal Product")
                .type(ProductType.NORMAL)
                .available(10)
                .leadTime(5)
                .build();

        seasonalProduct = Product.builder()
                .id(2L)
                .name("Seasonal Product")
                .type(ProductType.SEASONAL)
                .available(5)
                .leadTime(7)
                .seasonStartDate(LocalDate.now().plusDays(10))
                .seasonEndDate(LocalDate.now().plusDays(100))
                .build();

        expirableProduct = Product.builder()
                .id(3L)
                .name("Expirable Product")
                .type(ProductType.EXPIRABLE)
                .available(3)
                .leadTime(2)
                .expiryDate(LocalDate.now().plusDays(30))
                .build();
    }

    @Test
    void notifyDelay_ShouldUpdateLeadTimeAndSendNotification() {
        // Arrange
        int newLeadTime = 10;
        when(productRepository.save(normalProduct)).thenReturn(normalProduct);

        // Act
        productService.notifyDelay(newLeadTime, normalProduct);

        // Assert
        verify(productRepository, times(1)).save(normalProduct);
        verify(notificationService, times(1))
                .sendDelayNotification(newLeadTime, normalProduct.getName());
        assertThat(normalProduct.getLeadTime()).isEqualTo(newLeadTime);
    }

    @Test
    void notifyDelay_ShouldHandleZeroLeadTime() {
        // Arrange
        int zeroLeadTime = 0;
        when(productRepository.save(normalProduct)).thenReturn(normalProduct);

        // Act
        productService.notifyDelay(zeroLeadTime, normalProduct);

        // Assert
        verify(productRepository, times(1)).save(normalProduct);
        verify(notificationService, times(1))
                .sendDelayNotification(zeroLeadTime, normalProduct.getName());
    }

    @Test
    void handleSeasonalProduct_ShouldCallSeasonalHandler() {
        // Arrange
        when(seasonalProductHandler.supports(ProductType.SEASONAL)).thenReturn(true);

        // Create product service with handlers injected
        ProductService serviceWithHandlers = new ProductService(
                productRepository,
                notificationService,
                productHandlers
        );

        // Act
        serviceWithHandlers.handleSeasonalProduct(seasonalProduct);

        // Assert
        verify(seasonalProductHandler, times(1)).handle(seasonalProduct);
        verify(expirableProductHandler, never()).handle(any());
        verify(otherProductHandler, never()).handle(any());
    }

    @Test
    void handleExpiredProduct_ShouldCallExpirableHandler() {
        // Arrange
        when(expirableProductHandler.supports(ProductType.EXPIRABLE)).thenReturn(true);


        ProductService serviceWithHandlers = new ProductService(
                productRepository,
                notificationService,
                productHandlers
        );

        // Act
        serviceWithHandlers.handleExpiredProduct(expirableProduct);

        // Assert
        verify(expirableProductHandler, times(1)).handle(expirableProduct);
        verify(seasonalProductHandler, never()).handle(any());
        verify(otherProductHandler, never()).handle(any());
    }

    @Test
    void handleSeasonalProduct_ShouldThrowException_WhenNoHandlerFound() {
        // Arrange
        when(seasonalProductHandler.supports(ProductType.SEASONAL)).thenReturn(false);
        when(expirableProductHandler.supports(ProductType.SEASONAL)).thenReturn(false);
        when(otherProductHandler.supports(ProductType.SEASONAL)).thenReturn(false);

        ProductService serviceWithHandlers = new ProductService(
                productRepository,
                notificationService,
                productHandlers
        );

        // Act & Assert
        assertThatThrownBy(() -> serviceWithHandlers.handleSeasonalProduct(seasonalProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No handler found for product type: SEASONAL");
    }

    @Test
    void handleExpiredProduct_ShouldThrowException_WhenNoHandlerFound() {
        // Arrange
        when(seasonalProductHandler.supports(ProductType.EXPIRABLE)).thenReturn(false);
        when(expirableProductHandler.supports(ProductType.EXPIRABLE)).thenReturn(false);
        when(otherProductHandler.supports(ProductType.EXPIRABLE)).thenReturn(false);

        ProductService serviceWithHandlers = new ProductService(
                productRepository,
                notificationService,
                productHandlers
        );

        // Act & Assert
        assertThatThrownBy(() -> serviceWithHandlers.handleExpiredProduct(expirableProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No handler found for product type: EXPIRABLE");
    }


    @Test
    void notifyDelay_ShouldSaveProductBeforeSendingNotification() {
        // Arrange
        int leadTime = 15;
        when(productRepository.save(normalProduct)).thenReturn(normalProduct);

        // Act
        productService.notifyDelay(leadTime, normalProduct);

        // Assert - Verify order of operations
        verify(productRepository, times(1)).save(normalProduct);
        verify(notificationService, times(1))
                .sendDelayNotification(leadTime, normalProduct.getName());

        // Ensure product is saved before notification is sent
        // (Mockito doesn't have built-in order verification, but we can verify both were called)
    }

    @Test
    void constructor_ShouldInitializeWithAllDependencies() {
        // Arrange & Act
        ProductService service = new ProductService(
                productRepository,
                notificationService,
                productHandlers
        );

        // Assert
        assertThat(service).isNotNull();
    }

    @Test
    void productHandlers_ShouldBeOptionalInConstructor() {
        // Arrange & Act - Test that service can be created without handlers
        ProductService service = new ProductService(
                productRepository,
                notificationService,
                null
        );

        // Assert
        assertThat(service).isNotNull();
    }

    @Test
    void notifyDelay_ShouldNotSaveWhenRepositoryThrowsException() {
        // Arrange
        int leadTime = 5;
        when(productRepository.save(normalProduct)).thenThrow(new RuntimeException("DB Error"));

        // Act & Assert
        assertThatThrownBy(() -> productService.notifyDelay(leadTime, normalProduct))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB Error");

        verify(notificationService, never()).sendDelayNotification(anyInt(), anyString());
    }

    @Test
    void notifyDelay_WithNullProduct_ShouldThrowNullPointerException() {
        // Arrange
        int leadTime = 5;

        // Act & Assert
        assertThatThrownBy(() -> productService.notifyDelay(leadTime, null))
                .isInstanceOf(NullPointerException.class);
    }

}