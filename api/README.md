# Merjane - Inventory Management System (Refactored)

## Changes Summary

1. Product Entity

Before: 

`
@Column(name = "type")
private String type;
`

After:

`
@Enumerated(EnumType.STRING)
@Column(name = "type")
private ProductType type;
@Builder
`

2. ProductType Enum

Created:

`
package com.nimbleways.springclean.enums;
public enum ProductType {
NORMAL,
SEASONAL,
EXPIRABLE
}
`

3. Strategy Pattern for Product Handling
   
Created Interface:

`
public interface ProductHandler {
void handle(Product product);
boolean supports(ProductType productType);
}
`

4. OrderProcessingService Extraction

`
@Service
@RequiredArgsConstructor
public class OrderProcessingService {
public void processOrderItem(Product product) {
switch (product.getType()) {
case NORMAL -> processNormalProduct(product);
case SEASONAL -> processSeasonalProduct(product);
case EXPIRABLE -> processExpirableProduct(product);
    }
  }
}
`

5. Controller Refactoring

Refactored to respect SRP

6. ProductService Refactoring

Solve SRP and OCP violation

7. Solve Naming convention & Testability Violation

Before: 

`
@Autowired ProductRepository pr;
`

After:

`
@RequiredArgsConstructor
private final ProductRepository productRepository;
private final List<ProductHandler> productHandlers;
`
## Tests

* To run tests (from the `api` subdirectory):
    * Unit tests: `./mvnw test`
    * Integration tests: `./mvnw integration-test`
    * All tests: `./mvnw verify`