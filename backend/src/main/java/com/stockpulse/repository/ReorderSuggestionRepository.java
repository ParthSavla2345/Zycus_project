package com.stockpulse.repository;

import com.stockpulse.domain.ReorderSuggestion;
import com.stockpulse.domain.SuggestionStatus;
import com.stockpulse.domain.TriggerReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReorderSuggestionRepository extends JpaRepository<ReorderSuggestion, Long> {

    List<ReorderSuggestion> findByStatusOrderByCreatedAtDesc(SuggestionStatus status);

    List<ReorderSuggestion> findByProductIdOrderByCreatedAtDesc(Long productId);

    Optional<ReorderSuggestion> findByProductIdAndTriggerReasonAndStatus(
            Long productId, TriggerReason triggerReason, SuggestionStatus status);

    List<ReorderSuggestion> findAllByOrderByCreatedAtDesc();
}
