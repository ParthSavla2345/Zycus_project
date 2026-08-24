package com.stockpulse.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank
    private String sku;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private Category category;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal currentPrice;

    @Column(nullable = false)
    @NotNull
    @Min(0)
    private Integer stockLevel;

    @Column(nullable = false)
    @NotNull
    @Min(0)
    private Integer reorderThreshold;

    /**
     * Demand velocity: orders per day (or per period).
     * Updated on each order event.
     */
    @Column(nullable = false)
    @NotNull
    @DecimalMin(value = "0.0")
    private Double demandVelocity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private ProductStatus status;

    /** Optional cost price for margin calculations */
    @Column(precision = 10, scale = 2)
    private BigDecimal costPrice;

    /** Optional supplier identifier */
    private String supplierId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum Category {
        ELECTRONICS, APPAREL, HOME
    }

    public enum ProductStatus {
        ACTIVE, OUT_OF_STOCK, PRICE_REVIEW_PENDING
    }
}
