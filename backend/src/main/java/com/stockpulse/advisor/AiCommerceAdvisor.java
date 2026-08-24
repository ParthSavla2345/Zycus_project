package com.stockpulse.advisor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockpulse.domain.Direction;
import com.stockpulse.domain.Product;
import com.stockpulse.domain.StrategyUsed;
import com.stockpulse.domain.TriggerReason;
import com.stockpulse.llm.LlmClient;
import com.stockpulse.llm.LlmException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiCommerceAdvisor implements CommerceAdvisor {

    private final LlmClient llmClient;
    private final RuleBasedCommerceAdvisor fallback;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT =
            "You are an expert retail pricing and inventory advisor. " +
            "You must respond with ONLY valid JSON — no markdown, no explanation, no code blocks. " +
            "The JSON must exactly match the required schema.";

    @Override
    public CommerceRecommendation recommend(Product product, TriggerReason triggerReason, double categoryAvgVelocity) {
        try {
            String userPrompt = buildPrompt(product, triggerReason, categoryAvgVelocity);
            String rawResponse = llmClient.complete(SYSTEM_PROMPT, userPrompt);
            CommerceRecommendation result = parseAndValidate(rawResponse);
            log.info("AI recommendation generated for product {} trigger {}", product.getSku(), triggerReason);
            return result;
        } catch (LlmException e) {
            log.warn("AI advisor failed for product {} ({}), falling back to rules: {}",
                    product.getSku(), triggerReason, e.getMessage());
            return fallback.recommend(product, triggerReason, categoryAvgVelocity);
        } catch (Exception e) {
            log.warn("Unexpected AI advisor error for product {}, falling back to rules: {}",
                    product.getSku(), e.getMessage());
            return fallback.recommend(product, triggerReason, categoryAvgVelocity);
        }
    }

    private String buildPrompt(Product product, TriggerReason triggerReason, double categoryAvgVelocity) {
        String triggerContext = switch (triggerReason) {
            case INVENTORY_LOW -> String.format(
                    "ALERT: Stock is BELOW the reorder threshold. Current stock (%d) is below the reorder threshold (%d). " +
                    "Reason for recommendation: INVENTORY_LOW. " +
                    "Your goal is to protect remaining inventory by recommending a modest price increase, " +
                    "and recommend replenishment to restore healthy stock levels.",
                    product.getStockLevel(), product.getReorderThreshold());
            case DEMAND_SPIKE -> String.format(
                    "ALERT: Demand velocity is unusually HIGH. Product velocity (%.1f) significantly exceeds " +
                    "the category average velocity (%.1f) — more than 2x. " +
                    "Reason for recommendation: DEMAND_SPIKE. " +
                    "Recommend a modest price adjustment to capture value from the demand surge, " +
                    "and recommend replenishment to meet elevated demand.",
                    product.getDemandVelocity(), categoryAvgVelocity);
            default -> "Reason for recommendation: MANUAL review requested. " +
                    "Provide a balanced recommendation based on current product metrics.";
        };

        return String.format("""
                %s

                Product details:
                - Name: %s
                - SKU: %s
                - Category: %s
                - Current Price: $%.2f
                - Current Stock: %d units
                - Reorder Threshold: %d units
                - Demand Velocity: %.1f orders/day
                - Category Average Velocity: %.1f orders/day

                Respond with ONLY this JSON structure (no other text):
                {
                  "pricing": {
                    "recommendedPrice": <number>,
                    "direction": "<INCREASE|DECREASE|HOLD>",
                    "confidence": <0.0-1.0>,
                    "reasoning": "<concise explanation under 200 chars>"
                  },
                  "reorder": {
                    "recommendedQuantity": <integer >= 1>,
                    "suggestedLeadTimeDays": <integer >= 1>,
                    "confidence": <0.0-1.0>,
                    "reasoning": "<concise explanation under 200 chars>"
                  }
                }
                """,
                triggerContext,
                product.getName(),
                product.getSku(),
                product.getCategory(),
                product.getCurrentPrice(),
                product.getStockLevel(),
                product.getReorderThreshold(),
                product.getDemandVelocity(),
                categoryAvgVelocity);
    }

    private CommerceRecommendation parseAndValidate(String rawResponse) {
        try {
            String cleaned = rawResponse.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```[a-z]*\\n?", "").replace("```", "").trim();
            }

            JsonNode root = objectMapper.readTree(cleaned);

            // Pricing validation
            JsonNode pricingNode = root.path("pricing");
            if (pricingNode.isMissingNode()) throw new LlmException("Missing 'pricing' field in LLM response");

            double rawPrice = pricingNode.path("recommendedPrice").asDouble(-1);
            if (rawPrice <= 0) throw new LlmException("recommendedPrice must be positive, got: " + rawPrice);

            String directionStr = pricingNode.path("direction").asText("");
            Direction direction;
            try {
                direction = Direction.valueOf(directionStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new LlmException("Invalid direction value: " + directionStr);
            }

            double pricingConfidence = pricingNode.path("confidence").asDouble(-1);
            if (pricingConfidence < 0 || pricingConfidence > 1)
                throw new LlmException("pricing.confidence out of range: " + pricingConfidence);

            String pricingReasoning = pricingNode.path("reasoning").asText("").trim();
            if (pricingReasoning.isBlank()) throw new LlmException("pricing.reasoning is missing");

            // Reorder validation
            JsonNode reorderNode = root.path("reorder");
            if (reorderNode.isMissingNode()) throw new LlmException("Missing 'reorder' field in LLM response");

            int quantity = reorderNode.path("recommendedQuantity").asInt(-1);
            if (quantity < 1) throw new LlmException("recommendedQuantity must be >= 1, got: " + quantity);

            int leadTime = reorderNode.path("suggestedLeadTimeDays").asInt(7);
            if (leadTime < 1) leadTime = 7;

            double reorderConfidence = reorderNode.path("confidence").asDouble(-1);
            if (reorderConfidence < 0 || reorderConfidence > 1)
                throw new LlmException("reorder.confidence out of range: " + reorderConfidence);

            String reorderReasoning = reorderNode.path("reasoning").asText("").trim();
            if (reorderReasoning.isBlank()) throw new LlmException("reorder.reasoning is missing");

            PricingRecommendation pricing = new PricingRecommendation(
                    BigDecimal.valueOf(rawPrice).setScale(2, java.math.RoundingMode.HALF_UP),
                    direction,
                    pricingConfidence,
                    pricingReasoning,
                    StrategyUsed.AI
            );

            ReorderRecommendation reorder = new ReorderRecommendation(
                    quantity,
                    leadTime,
                    reorderConfidence,
                    reorderReasoning,
                    StrategyUsed.AI
            );

            return new CommerceRecommendation(pricing, reorder);

        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmException("Failed to parse/validate LLM JSON response: " + e.getMessage(), e);
        }
    }
}
