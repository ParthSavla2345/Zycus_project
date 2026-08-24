package com.stockpulse.dto;

import com.stockpulse.domain.Product;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        Product.Category category,
        BigDecimal currentPrice,
        Integer stockLevel,
        Integer reorderThreshold,
        Double demandVelocity,
        Product.ProductStatus status,
        BigDecimal costPrice,
        String supplierId,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(), p.getSku(), p.getName(), p.getCategory(),
                p.getCurrentPrice(), p.getStockLevel(), p.getReorderThreshold(),
                p.getDemandVelocity(), p.getStatus(), p.getCostPrice(),
                p.getSupplierId(), p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
