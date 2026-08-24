package com.stockpulse.event;

import com.stockpulse.advisor.CommerceAdvisorFactory;
import com.stockpulse.advisor.CommerceRecommendation;
import com.stockpulse.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async listener for StockSignalEvent.
 *
 * Runs in a separate thread pool — the HTTP order request has already returned
 * by the time this executes. Creates BOTH pricing and reorder suggestions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StockSignalListener {

    private final CommerceAdvisorFactory advisorFactory;
    private final SuggestionService suggestionService;

    @Async
    @EventListener
    public void handleStockSignal(StockSignalEvent event) {
        log.info("[ASYNC] Processing signal {} for product {}",
                event.getTriggerReason(), event.getProduct().getSku());
        try {
            CommerceRecommendation recommendation = advisorFactory.getAdvisor()
                    .recommend(event.getProduct(), event.getTriggerReason(), event.getCategoryAvgVelocity());

            suggestionService.createSuggestionsFromRecommendation(
                    event.getProduct(), recommendation, event.getTriggerReason());

            log.info("[ASYNC] Suggestions created for product {} trigger {}",
                    event.getProduct().getSku(), event.getTriggerReason());
        } catch (Exception e) {
            log.error("[ASYNC] Failed to process signal for product {}: {}",
                    event.getProduct().getSku(), e.getMessage(), e);
        }
    }
}
