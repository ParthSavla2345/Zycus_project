package com.stockpulse.advisor;

import com.stockpulse.domain.Direction;
import com.stockpulse.domain.Product;
import com.stockpulse.domain.StrategyUsed;
import com.stockpulse.domain.TriggerReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RuleBasedCommerceAdvisorTest {

    private RuleBasedCommerceAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new RuleBasedCommerceAdvisor();
    }

    @Test
    @DisplayName("1. Rule Pricing - Stock below threshold causes 10% price increase")
    void testInventoryLowPricingIncrease() {
        Product product = Product.builder()
                .sku("PRD-003")
                .name("Organic Cotton T-Shirt")
                .category(Product.Category.APPAREL)
                .currentPrice(new BigDecimal("20.00"))
                .stockLevel(5)
                .reorderThreshold(10)
                .demandVelocity(5.0)
                .build();

        CommerceRecommendation rec = advisor.recommend(product, TriggerReason.INVENTORY_LOW, 6.0);

        assertEquals(new BigDecimal("22.00"), rec.pricing().recommendedPrice());
        assertEquals(Direction.INCREASE, rec.pricing().direction());
        assertEquals(StrategyUsed.RULE, rec.pricing().strategyUsed());
        assertTrue(rec.pricing().reasoning().contains("below reorder threshold"));
    }

    @Test
    @DisplayName("4. Rule Pricing - Demand velocity > 2x category average causes 5% price increase")
    void testDemandSpikePricingIncrease() {
        Product product = Product.builder()
                .sku("PRD-001")
                .name("Tech Item")
                .category(Product.Category.ELECTRONICS)
                .currentPrice(new BigDecimal("100.00"))
                .stockLevel(50)
                .reorderThreshold(20)
                .demandVelocity(15.0)
                .build();

        CommerceRecommendation rec = advisor.recommend(product, TriggerReason.DEMAND_SPIKE, 5.0);

        assertEquals(new BigDecimal("105.00"), rec.pricing().recommendedPrice());
        assertEquals(Direction.INCREASE, rec.pricing().direction());
        assertEquals(StrategyUsed.RULE, rec.pricing().strategyUsed());
    }

    @Test
    @DisplayName("Rule Pricing - Normal conditions causes HOLD with current price")
    void testNormalPricingHold() {
        Product product = Product.builder()
                .sku("PRD-005")
                .name("Home Item")
                .category(Product.Category.HOME)
                .currentPrice(new BigDecimal("50.00"))
                .stockLevel(50)
                .reorderThreshold(20)
                .demandVelocity(4.0)
                .build();

        CommerceRecommendation rec = advisor.recommend(product, TriggerReason.MANUAL, 4.0);

        assertEquals(new BigDecimal("50.00"), rec.pricing().recommendedPrice());
        assertEquals(Direction.HOLD, rec.pricing().direction());
    }

    @Test
    @DisplayName("2. Rule Reorder - Calculates max(1, (threshold * 3) - currentStock)")
    void testReorderCalculation() {
        Product product = Product.builder()
                .sku("PRD-003")
                .stockLevel(8)
                .reorderThreshold(15)
                .demandVelocity(10.0)
                .currentPrice(new BigDecimal("25.00"))
                .build();

        CommerceRecommendation rec = advisor.recommend(product, TriggerReason.INVENTORY_LOW, 10.0);

        assertEquals(37, rec.reorder().recommendedQuantity());
        assertEquals(StrategyUsed.RULE, rec.reorder().strategyUsed());
    }
}
