package com.stockpulse.dto;

import com.stockpulse.domain.ReorderSuggestion;
import com.stockpulse.domain.StrategyUsed;
import com.stockpulse.domain.SuggestionStatus;
import com.stockpulse.domain.TriggerReason;

import java.time.Instant;

public record ReorderSuggestionResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Integer currentStock,
        Integer recommendedQuantity,
        Integer suggestedLeadTimeDays,
        Double confidence,
        String reasoning,
        SuggestionStatus status,
        TriggerReason triggerReason,
        StrategyUsed strategyUsed,
        Instant createdAt
) {
    public static ReorderSuggestionResponse from(ReorderSuggestion s) {
        return new ReorderSuggestionResponse(
                s.getId(),
                s.getProduct().getId(),
                s.getProduct().getName(),
                s.getProduct().getSku(),
                s.getCurrentStock(),
                s.getRecommendedQuantity(),
                s.getSuggestedLeadTimeDays(),
                s.getConfidence(),
                s.getReasoning(),
                s.getStatus(),
                s.getTriggerReason(),
                s.getStrategyUsed(),
                s.getCreatedAt()
        );
    }
}
