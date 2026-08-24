package com.stockpulse.dto;

import com.stockpulse.domain.Product;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        @NotNull Product.Category category,
        @NotNull @DecimalMin("0.01") BigDecimal currentPrice,
        @NotNull @Min(0) Integer stockLevel,
        @NotNull @Min(0) Integer reorderThreshold,
        @NotNull @DecimalMin("0.0") Double demandVelocity,
        BigDecimal costPrice,
        String supplierId
) {}
