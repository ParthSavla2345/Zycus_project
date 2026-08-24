package com.stockpulse.advisor;

import com.stockpulse.domain.Direction;
import com.stockpulse.domain.StrategyUsed;

import java.math.BigDecimal;

public record PricingRecommendation(
        BigDecimal recommendedPrice,
        Direction direction,
        double confidence,
        String reasoning,
        StrategyUsed strategyUsed
) {}
