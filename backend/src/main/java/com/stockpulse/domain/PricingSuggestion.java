package com.stockpulse.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pricing_suggestions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    private BigDecimal currentPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal recommendedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private Direction direction;

    @Column(nullable = false)
    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private Double confidence;

    @Column(nullable = false, length = 1000)
    @NotBlank
    private String reasoning;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private SuggestionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private TriggerReason triggerReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private StrategyUsed strategyUsed;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (status == null) status = SuggestionStatus.PENDING;
    }
}
