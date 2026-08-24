package com.stockpulse.advisor;

import com.stockpulse.domain.Direction;
import com.stockpulse.domain.Product;
import com.stockpulse.domain.StrategyUsed;
import com.stockpulse.domain.TriggerReason;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class RuleBasedCommerceAdvisor implements CommerceAdvisor {

    @Override
    public CommerceRecommendation recommend(Product product, TriggerReason triggerReason, double categoryAvgVelocity) {
        return new CommerceRecommendation(
                buildPricing(product, categoryAvgVelocity),
                buildReorder(product)
        );
    }

    private PricingRecommendation buildPricing(Product product, double categoryAvgVelocity) {
        BigDecimal currentPrice = product.getCurrentPrice();
        BigDecimal recommendedPrice;
        Direction direction;
        double confidence;
        String reasoning;

        if (product.getStockLevel() < product.getReorderThreshold()) {
            recommendedPrice = currentPrice.multiply(new BigDecimal("1.10"))
                    .setScale(2, RoundingMode.HALF_UP);
            direction = Direction.INCREASE;
            confidence = 0.85;
            reasoning = String.format(
                    "Stock level (%d) is below reorder threshold (%d). Applying a 10%% price increase to protect remaining inventory and signal scarcity.",
                    product.getStockLevel(), product.getReorderThreshold());
        } else if (categoryAvgVelocity > 0 && product.getDemandVelocity() > 2 * categoryAvgVelocity) {
            recommendedPrice = currentPrice.multiply(new BigDecimal("1.05"))
                    .setScale(2, RoundingMode.HALF_UP);
            direction = Direction.INCREASE;
            confidence = 0.80;
            reasoning = String.format(
                    "Demand velocity (%.1f) exceeds 2x category average (%.1f). Applying a modest 5%% price increase to capture demand while remaining competitive.",
                    product.getDemandVelocity(), categoryAvgVelocity);
        } else {
            recommendedPrice = currentPrice;
            direction = Direction.HOLD;
            confidence = 0.75;
            reasoning = String.format(
                    "Stock level (%d) is healthy and demand velocity (%.1f) is within normal range. No price change recommended.",
                    product.getStockLevel(), product.getDemandVelocity());
        }

        return new PricingRecommendation(recommendedPrice, direction, confidence, reasoning,
                StrategyUsed.RULE);
    }

    private ReorderRecommendation buildReorder(Product product) {
        int recommended = Math.max(1, (product.getReorderThreshold() * 3) - product.getStockLevel());
        String reasoning = String.format(
                "Recommended order of %d units to bring inventory to 3x reorder threshold (%d). Current stock: %d.",
                recommended, product.getReorderThreshold(), product.getStockLevel());

        return new ReorderRecommendation(
                recommended,
                7,
                0.90,
                reasoning,
                StrategyUsed.RULE
        );
    }
}
