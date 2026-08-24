package com.stockpulse.service;

import com.stockpulse.advisor.CommerceRecommendation;
import com.stockpulse.advisor.PricingRecommendation;
import com.stockpulse.advisor.ReorderRecommendation;
import com.stockpulse.domain.*;
import com.stockpulse.dto.PricingSuggestionResponse;
import com.stockpulse.dto.ReorderSuggestionResponse;
import com.stockpulse.dto.SuggestionActionRequest;
import com.stockpulse.exception.ConflictException;
import com.stockpulse.repository.PricingSuggestionRepository;
import com.stockpulse.repository.ProductRepository;
import com.stockpulse.repository.ReorderSuggestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SuggestionServiceIntegrationTest {

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PricingSuggestionRepository pricingSuggestionRepository;

    @Autowired
    private ReorderSuggestionRepository reorderSuggestionRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        pricingSuggestionRepository.deleteAll();
        reorderSuggestionRepository.deleteAll();

        testProduct = productRepository.findBySku("PRD-003")
                .orElseGet(() -> productRepository.save(Product.builder()
                        .sku("PRD-003")
                        .name("Organic Cotton T-Shirt")
                        .category(Product.Category.APPAREL)
                        .currentPrice(new BigDecimal("24.99"))
                        .stockLevel(8)
                        .reorderThreshold(15)
                        .demandVelocity(12.0)
                        .status(Product.ProductStatus.ACTIVE)
                        .build()));
    }

    @Test
    @DisplayName("5. Accept pricing updates Product.currentPrice to recommendedPrice")
    void testAcceptPricingUpdatesProductPrice() {
        PricingSuggestion suggestion = pricingSuggestionRepository.save(PricingSuggestion.builder()
                .product(testProduct)
                .currentPrice(testProduct.getCurrentPrice())
                .recommendedPrice(new BigDecimal("27.49"))
                .direction(Direction.INCREASE)
                .confidence(0.85)
                .reasoning("Inventory low price adjustment")
                .status(SuggestionStatus.PENDING)
                .triggerReason(TriggerReason.INVENTORY_LOW)
                .strategyUsed(StrategyUsed.RULE)
                .build());

        PricingSuggestionResponse response = suggestionService.processPricingAction(
                suggestion.getId(), new SuggestionActionRequest(SuggestionActionRequest.Action.ACCEPT));

        assertEquals(SuggestionStatus.ACCEPTED, response.status());

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(new BigDecimal("27.49"), updated.getCurrentPrice());
    }

    @Test
    @DisplayName("Reject pricing marks suggestion REJECTED without modifying product price")
    void testRejectPricingLeavesPriceUnchanged() {
        BigDecimal initialPrice = testProduct.getCurrentPrice();
        PricingSuggestion suggestion = pricingSuggestionRepository.save(PricingSuggestion.builder()
                .product(testProduct)
                .currentPrice(initialPrice)
                .recommendedPrice(new BigDecimal("35.00"))
                .direction(Direction.INCREASE)
                .confidence(0.85)
                .reasoning("Price adjustment")
                .status(SuggestionStatus.PENDING)
                .triggerReason(TriggerReason.INVENTORY_LOW)
                .strategyUsed(StrategyUsed.RULE)
                .build());

        PricingSuggestionResponse response = suggestionService.processPricingAction(
                suggestion.getId(), new SuggestionActionRequest(SuggestionActionRequest.Action.REJECT));

        assertEquals(SuggestionStatus.REJECTED, response.status());

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(initialPrice, updated.getCurrentPrice());
    }

    @Test
    @DisplayName("6. Accept reorder increases Product.stockLevel by recommendedQuantity")
    void testAcceptReorderIncreasesStock() {
        int initialStock = testProduct.getStockLevel();
        ReorderSuggestion suggestion = reorderSuggestionRepository.save(ReorderSuggestion.builder()
                .product(testProduct)
                .currentStock(initialStock)
                .recommendedQuantity(37)
                .suggestedLeadTimeDays(7)
                .confidence(0.9)
                .reasoning("Replenish stock")
                .status(SuggestionStatus.PENDING)
                .triggerReason(TriggerReason.INVENTORY_LOW)
                .strategyUsed(StrategyUsed.RULE)
                .build());

        ReorderSuggestionResponse response = suggestionService.processReorderAction(
                suggestion.getId(), new SuggestionActionRequest(SuggestionActionRequest.Action.ACCEPT));

        assertEquals(SuggestionStatus.ACCEPTED, response.status());

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(initialStock + 37, updated.getStockLevel());
    }

    @Test
    @DisplayName("7. Duplicate PENDING prevention (Idempotency)")
    void testDuplicatePendingPrevention() {
        CommerceRecommendation rec1 = new CommerceRecommendation(
                new PricingRecommendation(new BigDecimal("27.49"), Direction.INCREASE, 0.85, "Reason 1", StrategyUsed.RULE),
                new ReorderRecommendation(37, 7, 0.9, "Reorder 1", StrategyUsed.RULE)
        );

        // First creation
        suggestionService.createSuggestionsFromRecommendation(testProduct, rec1, TriggerReason.INVENTORY_LOW);

        List<PricingSuggestion> pricingList1 = pricingSuggestionRepository.findByStatusOrderByCreatedAtDesc(SuggestionStatus.PENDING);
        List<ReorderSuggestion> reorderList1 = reorderSuggestionRepository.findByStatusOrderByCreatedAtDesc(SuggestionStatus.PENDING);
        assertEquals(1, pricingList1.size());
        assertEquals(1, reorderList1.size());

        // Attempt second creation with SAME product and triggerReason
        CommerceRecommendation rec2 = new CommerceRecommendation(
                new PricingRecommendation(new BigDecimal("27.49"), Direction.INCREASE, 0.85, "Reason 2", StrategyUsed.RULE),
                new ReorderRecommendation(37, 7, 0.9, "Reorder 2", StrategyUsed.RULE)
        );
        suggestionService.createSuggestionsFromRecommendation(testProduct, rec2, TriggerReason.INVENTORY_LOW);

        // Count must still be 1 (no duplicate pending created)
        List<PricingSuggestion> pricingList2 = pricingSuggestionRepository.findByStatusOrderByCreatedAtDesc(SuggestionStatus.PENDING);
        List<ReorderSuggestion> reorderList2 = reorderSuggestionRepository.findByStatusOrderByCreatedAtDesc(SuggestionStatus.PENDING);
        assertEquals(1, pricingList2.size());
        assertEquals(1, reorderList2.size());
    }

    @Test
    @DisplayName("Cannot re-accept or re-reject an already resolved suggestion")
    void testCannotReResolveSuggestion() {
        PricingSuggestion suggestion = pricingSuggestionRepository.save(PricingSuggestion.builder()
                .product(testProduct)
                .currentPrice(testProduct.getCurrentPrice())
                .recommendedPrice(new BigDecimal("27.49"))
                .direction(Direction.INCREASE)
                .confidence(0.85)
                .reasoning("Price adjustment")
                .status(SuggestionStatus.PENDING)
                .triggerReason(TriggerReason.INVENTORY_LOW)
                .strategyUsed(StrategyUsed.RULE)
                .build());

        // First accept
        suggestionService.processPricingAction(
                suggestion.getId(), new SuggestionActionRequest(SuggestionActionRequest.Action.ACCEPT));

        // Second accept should throw ConflictException
        assertThrows(ConflictException.class, () -> suggestionService.processPricingAction(
                suggestion.getId(), new SuggestionActionRequest(SuggestionActionRequest.Action.ACCEPT)));
    }
}
