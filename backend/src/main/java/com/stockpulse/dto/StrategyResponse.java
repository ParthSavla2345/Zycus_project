package com.stockpulse.dto;

import com.stockpulse.config.StrategyConfig;

public record StrategyResponse(
        StrategyConfig.StrategyType strategy
) {}
