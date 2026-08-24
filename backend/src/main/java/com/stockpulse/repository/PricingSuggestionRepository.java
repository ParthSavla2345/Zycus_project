package com.stockpulse.repository;

import com.stockpulse.domain.PricingSuggestion;
import com.stockpulse.domain.SuggestionStatus;
import com.stockpulse.domain.TriggerReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingSuggestionRepository extends JpaRepository<PricingSuggestion, Long> {

    List<PricingSuggestion> findByStatusOrderByCreatedAtDesc(SuggestionStatus status);

    List<PricingSuggestion> findByProductIdOrderByCreatedAtDesc(Long productId);

    Optional<PricingSuggestion> findByProductIdAndTriggerReasonAndStatus(
            Long productId, TriggerReason triggerReason, SuggestionStatus status);

    List<PricingSuggestion> findAllByOrderByCreatedAtDesc();
}
