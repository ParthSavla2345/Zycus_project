package com.stockpulse.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "reorder_suggestions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReorderSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @NotNull
    @Min(0)
    private Integer currentStock;

    @Column(nullable = false)
    @NotNull
    @Min(1)
    private Integer recommendedQuantity;

    @Column(nullable = false)
    @NotNull
    @Min(1)
    private Integer suggestedLeadTimeDays;

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
