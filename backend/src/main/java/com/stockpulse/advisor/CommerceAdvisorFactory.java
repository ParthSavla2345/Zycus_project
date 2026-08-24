package com.stockpulse.advisor;

import com.stockpulse.config.StrategyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Selects the correct CommerceAdvisor based on the current runtime strategy.
 * The factory reads from StrategyConfig (AtomicReference) on every call,
 * so strategy changes take effect immediately without restart.
 */
@Component
@RequiredArgsConstructor
public class CommerceAdvisorFactory {

    private final StrategyConfig strategyConfig;
    private final RuleBasedCommerceAdvisor ruleBasedAdvisor;
    private final AiCommerceAdvisor aiAdvisor;

    public CommerceAdvisor getAdvisor() {
        return switch (strategyConfig.getStrategy()) {
            case AI -> aiAdvisor;
            case RULE -> ruleBasedAdvisor;
        };
    }

    public RuleBasedCommerceAdvisor getRuleAdvisor() {
        return ruleBasedAdvisor;
    }
}
