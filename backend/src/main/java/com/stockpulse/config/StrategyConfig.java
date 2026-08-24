package com.stockpulse.config;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Runtime strategy configuration.
 * Uses AtomicReference so strategy can be switched live without restart.
 */
@Component
public class StrategyConfig {

    public enum StrategyType {
        AI, RULE
    }

    /** Default strategy is AI; falls back to RULE if LLM fails */
    private final AtomicReference<StrategyType> currentStrategy =
            new AtomicReference<>(StrategyType.AI);

    public StrategyType getStrategy() {
        return currentStrategy.get();
    }

    public void setStrategy(StrategyType strategy) {
        currentStrategy.set(strategy);
    }
}
