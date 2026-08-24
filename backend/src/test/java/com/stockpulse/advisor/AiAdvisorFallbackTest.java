package com.stockpulse.advisor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockpulse.domain.Direction;
import com.stockpulse.domain.Product;
import com.stockpulse.domain.StrategyUsed;
import com.stockpulse.domain.TriggerReason;
import com.stockpulse.llm.LlmClient;
import com.stockpulse.llm.LlmException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AiAdvisorFallbackTest {

    private LlmClient llmClient;
    private RuleBasedCommerceAdvisor fallback;
    private AiCommerceAdvisor aiAdvisor;

    @BeforeEach
    void setUp() {
        llmClient = Mockito.mock(LlmClient.class);
        fallback = new RuleBasedCommerceAdvisor();
        aiAdvisor = new AiCommerceAdvisor(llmClient, fallback, new ObjectMapper());
    }

    @Test
    @DisplayName("8. AI invalid/failing response falls back to rules with strategyUsed = RULE")
    void testFallbackOnLlmFailure() {
        when(llmClient.complete(anyString(), anyString()))
                .thenThrow(new LlmException("Connection timeout to LiteLLM"));

        Product product = Product.builder()
                .sku("PRD-003")
                .name("Organic Cotton T-Shirt")
                .category(Product.Category.APPAREL)
                .currentPrice(new BigDecimal("20.00"))
                .stockLevel(5)
                .reorderThreshold(10)
                .demandVelocity(5.0)
                .build();

        CommerceRecommendation rec = aiAdvisor.recommend(product, TriggerReason.INVENTORY_LOW, 6.0);

        assertNotNull(rec);
        assertEquals(new BigDecimal("22.00"), rec.pricing().recommendedPrice());
        assertEquals(StrategyUsed.RULE, rec.pricing().strategyUsed());
        assertEquals(StrategyUsed.RULE, rec.reorder().strategyUsed());
    }

    @Test
    @DisplayName("AI invalid JSON structure falls back to rules")
    void testFallbackOnMalformedJson() {
        when(llmClient.complete(anyString(), anyString()))
                .thenReturn("Sorry, I cannot fulfill this request as JSON.");

        Product product = Product.builder()
                .sku("PRD-008")
                .name("Hoodie")
                .category(Product.Category.APPAREL)
                .currentPrice(new BigDecimal("50.00"))
                .stockLevel(11)
                .reorderThreshold(12)
                .demandVelocity(15.0)
                .build();

        CommerceRecommendation rec = aiAdvisor.recommend(product, TriggerReason.INVENTORY_LOW, 8.0);

        assertNotNull(rec);
        assertEquals(StrategyUsed.RULE, rec.pricing().strategyUsed());
    }

    @Test
    @DisplayName("AI valid response parses and returns strategyUsed = AI")
    void testValidAiResponse() {
        String validJson = """
                {
                  "pricing": {
                    "recommendedPrice": 29.99,
                    "direction": "INCREASE",
                    "confidence": 0.88,
                    "reasoning": "High demand spike warrants price adjustment"
                  },
                  "reorder": {
                    "recommendedQuantity": 40,
                    "suggestedLeadTimeDays": 5,
                    "confidence": 0.85,
                    "reasoning": "Restock to avoid stockout"
                  }
                }
                """;

        when(llmClient.complete(anyString(), anyString())).thenReturn(validJson);

        Product product = Product.builder()
                .sku("PRD-001")
                .name("Gadget")
                .category(Product.Category.ELECTRONICS)
                .currentPrice(new BigDecimal("25.00"))
                .stockLevel(30)
                .reorderThreshold(15)
                .demandVelocity(10.0)
                .build();

        CommerceRecommendation rec = aiAdvisor.recommend(product, TriggerReason.DEMAND_SPIKE, 4.0);

        assertNotNull(rec);
        assertEquals(new BigDecimal("29.99"), rec.pricing().recommendedPrice());
        assertEquals(Direction.INCREASE, rec.pricing().direction());
        assertEquals(StrategyUsed.AI, rec.pricing().strategyUsed());
        assertEquals(40, rec.reorder().recommendedQuantity());
        assertEquals(StrategyUsed.AI, rec.reorder().strategyUsed());
    }
}
