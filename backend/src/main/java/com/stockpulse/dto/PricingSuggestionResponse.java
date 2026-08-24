package com.stockpulse.dto;

import com.stockpulse.domain.Direction;
import com.stockpulse.domain.PricingSuggestion;
import com.stockpulse.domain.StrategyUsed;
import com.stockpulse.domain.SuggestionStatus;
import com.stockpulse.domain.TriggerReason;

import java.math.BigDecimal;
import java.time.Instant;

public record PricingSuggestionResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        BigDecimal currentPrice,
        BigDecimal recommendedPrice,
        Direction direction,
        Double confidence,
        String reasoning,
        SuggestionStatus status,
        TriggerReason triggerReason,
        StrategyUsed strategyUsed,
        Instant createdAt
) {
    public static PricingSuggestionResponse from(PricingSuggestion s) {
        return new PricingSuggestionResponse(
                s.getId(),
                s.getProduct().getId(),
                s.getProduct().getName(),
                s.getProduct().getSku(),
                s.getCurrentPrice(),
                s.getRecommendedPrice(),
                s.getDirection(),
                s.getConfidence(),
                s.getReasoning(),
                s.getStatus(),
                s.getTriggerReason(),
                s.getStrategyUsed(),
                s.getCreatedAt()
        );
    }
}
