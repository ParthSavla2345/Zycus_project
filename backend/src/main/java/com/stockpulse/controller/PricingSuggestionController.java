package com.stockpulse.controller;

import com.stockpulse.domain.SuggestionStatus;
import com.stockpulse.dto.PricingSuggestionResponse;
import com.stockpulse.dto.SuggestionActionRequest;
import com.stockpulse.service.SuggestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pricing-suggestions")
@RequiredArgsConstructor
public class PricingSuggestionController {

    private final SuggestionService suggestionService;

    @GetMapping
    public ResponseEntity<List<PricingSuggestionResponse>> getPricingSuggestions(
            @RequestParam(required = false) SuggestionStatus status) {
        return ResponseEntity.ok(suggestionService.getPricingSuggestions(status));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PricingSuggestionResponse> processAction(
            @PathVariable Long id,
            @Valid @RequestBody SuggestionActionRequest request) {
        return ResponseEntity.ok(suggestionService.processPricingAction(id, request));
    }
}
