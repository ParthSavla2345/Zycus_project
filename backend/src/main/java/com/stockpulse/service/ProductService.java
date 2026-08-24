package com.stockpulse.service;

import com.stockpulse.domain.TriggerReason;
import com.stockpulse.domain.Product;
import com.stockpulse.domain.Product.Category;
import com.stockpulse.domain.Product.ProductStatus;
import com.stockpulse.dto.CreateProductRequest;
import com.stockpulse.dto.OrderRequest;
import com.stockpulse.dto.ProductResponse;
import com.stockpulse.dto.UpdateStockRequest;
import com.stockpulse.event.StockSignalEvent;
import com.stockpulse.exception.ConflictException;
import com.stockpulse.exception.ResourceNotFoundException;
import com.stockpulse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts(Category category, String search) {
        List<Product> products;
        if (category != null) {
            products = productRepository.findByCategory(category);
        } else if (search != null && !search.isBlank()) {
            products = productRepository.findByNameContainingIgnoreCase(search.trim());
        } else {
            products = productRepository.findAll();
        }
        return products.stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        productRepository.findBySku(request.sku()).ifPresent(p -> {
            throw new ConflictException("Product with SKU already exists: " + request.sku());
        });

        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .category(request.category())
                .currentPrice(request.currentPrice())
                .stockLevel(request.stockLevel())
                .reorderThreshold(request.reorderThreshold())
                .demandVelocity(request.demandVelocity())
                .status(request.stockLevel() == 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE)
                .costPrice(request.costPrice())
                .supplierId(request.supplierId())
                .build();

        Product saved = productRepository.save(product);
        log.info("Created product: {} (SKU: {})", saved.getName(), saved.getSku());
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse updateStock(Long id, UpdateStockRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setStockLevel(request.stockLevel());
        if (product.getStockLevel() == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }

        Product saved = productRepository.save(product);
        log.info("Updated stock for product {}: new stock = {}", saved.getSku(), saved.getStockLevel());
        return ProductResponse.from(saved);
    }

    /**
     * Critical Agentic Async Loop:
     * 1. Decreases stock
     * 2. Increases / updates demand velocity
     * 3. Detects signals (INVENTORY_LOW, DEMAND_SPIKE)
     * 4. Publishes Spring ApplicationEvent asynchronously
     * 5. Returns immediate HTTP response (does NOT block on LLM)
     */
    @Transactional
    public ProductResponse processOrder(Long id, OrderRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        int orderedQty = request.quantity();
        if (product.getStockLevel() < orderedQty) {
            throw new ConflictException(String.format(
                    "Insufficient stock for product %s. Available: %d, Requested: %d",
                    product.getSku(), product.getStockLevel(), orderedQty));
        }

        // 1. Decrease stock
        product.setStockLevel(product.getStockLevel() - orderedQty);
        if (product.getStockLevel() == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        }

        // 2. Increase demand velocity (incremental boost based on order activity)
        double newVelocity = Math.round((product.getDemandVelocity() + (orderedQty * 1.5)) * 10.0) / 10.0;
        product.setDemandVelocity(newVelocity);

        Product savedProduct = productRepository.save(product);
        log.info("Processed order for product {}: remaining stock = {}, velocity = {}",
                savedProduct.getSku(), savedProduct.getStockLevel(), savedProduct.getDemandVelocity());

        // 3. Category average demand velocity calculation
        Double avgVelocity = productRepository.findAverageDemandVelocityByCategory(savedProduct.getCategory());
        double categoryAvg = avgVelocity != null ? avgVelocity : savedProduct.getDemandVelocity();

        // 4. Signal detection & async event publishing
        // Detect INVENTORY_LOW
        if (savedProduct.getStockLevel() < savedProduct.getReorderThreshold()) {
            log.info("Signal detected: INVENTORY_LOW for product {} (Stock: {} < Threshold: {})",
                    savedProduct.getSku(), savedProduct.getStockLevel(), savedProduct.getReorderThreshold());
            eventPublisher.publishEvent(new StockSignalEvent(
                    this, savedProduct, TriggerReason.INVENTORY_LOW, categoryAvg));
        }

        // Detect DEMAND_SPIKE (velocity > 2 * category average)
        if (categoryAvg > 0 && savedProduct.getDemandVelocity() > 2 * categoryAvg) {
            log.info("Signal detected: DEMAND_SPIKE for product {} (Velocity: {} > 2 * Avg: {})",
                    savedProduct.getSku(), savedProduct.getDemandVelocity(), categoryAvg);
            eventPublisher.publishEvent(new StockSignalEvent(
                    this, savedProduct, TriggerReason.DEMAND_SPIKE, categoryAvg));
        }

        // 5. Return immediately
        return ProductResponse.from(savedProduct);
    }
}
