package com.stockpulse.service;

import com.stockpulse.advisor.CommerceAdvisorFactory;
import com.stockpulse.advisor.CommerceRecommendation;
import com.stockpulse.advisor.PricingRecommendation;
import com.stockpulse.advisor.ReorderRecommendation;
import com.stockpulse.domain.*;
import com.stockpulse.dto.PricingSuggestionResponse;
import com.stockpulse.dto.ReorderSuggestionResponse;
import com.stockpulse.dto.SuggestionActionRequest;
import com.stockpulse.exception.ConflictException;
import com.stockpulse.exception.ResourceNotFoundException;
import com.stockpulse.repository.PricingSuggestionRepository;
import com.stockpulse.repository.ProductRepository;
import com.stockpulse.repository.ReorderSuggestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuggestionService {

    private final PricingSuggestionRepository pricingSuggestionRepository;
    private final ReorderSuggestionRepository reorderSuggestionRepository;
    private final ProductRepository productRepository;
    private final CommerceAdvisorFactory advisorFactory;

    @Transactional(readOnly = true)
    public List<PricingSuggestionResponse> getPricingSuggestions(SuggestionStatus status) {
        List<PricingSuggestion> list = status == null
                ? pricingSuggestionRepository.findAllByOrderByCreatedAtDesc()
                : pricingSuggestionRepository.findByStatusOrderByCreatedAtDesc(status);
        return list.stream().map(PricingSuggestionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReorderSuggestionResponse> getReorderSuggestions(SuggestionStatus status) {
        List<ReorderSuggestion> list = status == null
                ? reorderSuggestionRepository.findAllByOrderByCreatedAtDesc()
                : reorderSuggestionRepository.findByStatusOrderByCreatedAtDesc(status);
        return list.stream().map(ReorderSuggestionResponse::from).toList();
    }

    @Transactional
    public void createSuggestionsFromRecommendation(
            Product product, CommerceRecommendation recommendation, TriggerReason triggerReason) {

        // 1. Pricing Suggestion with Idempotency Check
        Optional<PricingSuggestion> existingPendingPricing = pricingSuggestionRepository
                .findByProductIdAndTriggerReasonAndStatus(product.getId(), triggerReason, SuggestionStatus.PENDING);

        if (existingPendingPricing.isPresent()) {
            log.info("Skipping duplicate PENDING pricing suggestion for product {} and trigger {}",
                    product.getSku(), triggerReason);
        } else {
            PricingRecommendation pRec = recommendation.pricing();
            PricingSuggestion pricingSuggestion = PricingSuggestion.builder()
                    .product(product)
                    .currentPrice(product.getCurrentPrice())
                    .recommendedPrice(pRec.recommendedPrice())
                    .direction(pRec.direction())
                    .confidence(pRec.confidence())
                    .reasoning(pRec.reasoning())
                    .status(SuggestionStatus.PENDING)
                    .triggerReason(triggerReason)
                    .strategyUsed(pRec.strategyUsed())
                    .build();
            pricingSuggestionRepository.save(pricingSuggestion);
            log.info("Saved PENDING pricing suggestion #{} for product {}", pricingSuggestion.getId(), product.getSku());
        }

        // 2. Reorder Suggestion with Idempotency Check
        Optional<ReorderSuggestion> existingPendingReorder = reorderSuggestionRepository
                .findByProductIdAndTriggerReasonAndStatus(product.getId(), triggerReason, SuggestionStatus.PENDING);

        if (existingPendingReorder.isPresent()) {
            log.info("Skipping duplicate PENDING reorder suggestion for product {} and trigger {}",
                    product.getSku(), triggerReason);
        } else {
            ReorderRecommendation rRec = recommendation.reorder();
            ReorderSuggestion reorderSuggestion = ReorderSuggestion.builder()
                    .product(product)
                    .currentStock(product.getStockLevel())
                    .recommendedQuantity(rRec.recommendedQuantity())
                    .suggestedLeadTimeDays(rRec.suggestedLeadTimeDays())
                    .confidence(rRec.confidence())
                    .reasoning(rRec.reasoning())
                    .status(SuggestionStatus.PENDING)
                    .triggerReason(triggerReason)
                    .strategyUsed(rRec.strategyUsed())
                    .build();
            reorderSuggestionRepository.save(reorderSuggestion);
            log.info("Saved PENDING reorder suggestion #{} for product {}", reorderSuggestion.getId(), product.getSku());
        }
    }

    @Transactional
    public PricingSuggestionResponse generateManualPricingSuggestion(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Double avgVelocity = productRepository.findAverageDemandVelocityByCategory(product.getCategory());
        double categoryAvg = avgVelocity != null ? avgVelocity : product.getDemandVelocity();

        CommerceRecommendation rec = advisorFactory.getAdvisor()
                .recommend(product, TriggerReason.MANUAL, categoryAvg);

        PricingRecommendation pRec = rec.pricing();
        PricingSuggestion suggestion = PricingSuggestion.builder()
                .product(product)
                .currentPrice(product.getCurrentPrice())
                .recommendedPrice(pRec.recommendedPrice())
                .direction(pRec.direction())
                .confidence(pRec.confidence())
                .reasoning(pRec.reasoning())
                .status(SuggestionStatus.PENDING)
                .triggerReason(TriggerReason.MANUAL)
                .strategyUsed(pRec.strategyUsed())
                .build();

        return PricingSuggestionResponse.from(pricingSuggestionRepository.save(suggestion));
    }

    @Transactional
    public ReorderSuggestionResponse generateManualReorderSuggestion(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Double avgVelocity = productRepository.findAverageDemandVelocityByCategory(product.getCategory());
        double categoryAvg = avgVelocity != null ? avgVelocity : product.getDemandVelocity();

        CommerceRecommendation rec = advisorFactory.getAdvisor()
                .recommend(product, TriggerReason.MANUAL, categoryAvg);

        ReorderRecommendation rRec = rec.reorder();
        ReorderSuggestion suggestion = ReorderSuggestion.builder()
                .product(product)
                .currentStock(product.getStockLevel())
                .recommendedQuantity(rRec.recommendedQuantity())
                .suggestedLeadTimeDays(rRec.suggestedLeadTimeDays())
                .confidence(rRec.confidence())
                .reasoning(rRec.reasoning())
                .status(SuggestionStatus.PENDING)
                .triggerReason(TriggerReason.MANUAL)
                .strategyUsed(rRec.strategyUsed())
                .build();

        return ReorderSuggestionResponse.from(reorderSuggestionRepository.save(suggestion));
    }

    @Transactional
    public PricingSuggestionResponse processPricingAction(Long id, SuggestionActionRequest request) {
        PricingSuggestion suggestion = pricingSuggestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing suggestion not found: " + id));

        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new ConflictException("Suggestion " + id + " is already " + suggestion.getStatus());
        }

        if (request.action() == SuggestionActionRequest.Action.ACCEPT) {
            suggestion.setStatus(SuggestionStatus.ACCEPTED);
            Product product = suggestion.getProduct();
            product.setCurrentPrice(suggestion.getRecommendedPrice());
            productRepository.save(product);
            log.info("Accepted pricing suggestion #{}: Product {} price updated from {} to {}",
                    id, product.getSku(), suggestion.getCurrentPrice(), suggestion.getRecommendedPrice());
        } else {
            suggestion.setStatus(SuggestionStatus.REJECTED);
            log.info("Rejected pricing suggestion #{} for Product {}", id, suggestion.getProduct().getSku());
        }

        return PricingSuggestionResponse.from(pricingSuggestionRepository.save(suggestion));
    }

    @Transactional
    public ReorderSuggestionResponse processReorderAction(Long id, SuggestionActionRequest request) {
        ReorderSuggestion suggestion = reorderSuggestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reorder suggestion not found: " + id));

        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new ConflictException("Suggestion " + id + " is already " + suggestion.getStatus());
        }

        if (request.action() == SuggestionActionRequest.Action.ACCEPT) {
            suggestion.setStatus(SuggestionStatus.ACCEPTED);
            Product product = suggestion.getProduct();
            product.setStockLevel(product.getStockLevel() + suggestion.getRecommendedQuantity());
            if (product.getStockLevel() > 0 && product.getStatus() == Product.ProductStatus.OUT_OF_STOCK) {
                product.setStatus(Product.ProductStatus.ACTIVE);
            }
            productRepository.save(product);
            log.info("Accepted reorder suggestion #{}: Product {} stock increased by {} to {}",
                    id, product.getSku(), suggestion.getRecommendedQuantity(), product.getStockLevel());
        } else {
            suggestion.setStatus(SuggestionStatus.REJECTED);
            log.info("Rejected reorder suggestion #{} for Product {}", id, suggestion.getProduct().getSku());
        }

        return ReorderSuggestionResponse.from(reorderSuggestionRepository.save(suggestion));
    }
}
