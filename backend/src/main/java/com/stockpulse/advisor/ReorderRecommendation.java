package com.stockpulse.advisor;

import com.stockpulse.domain.StrategyUsed;

public record ReorderRecommendation(
        int recommendedQuantity,
        int suggestedLeadTimeDays,
        double confidence,
        String reasoning,
        StrategyUsed strategyUsed
) {}
