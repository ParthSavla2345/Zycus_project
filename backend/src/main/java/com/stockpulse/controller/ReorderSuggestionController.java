package com.stockpulse.controller;

import com.stockpulse.domain.SuggestionStatus;
import com.stockpulse.dto.ReorderSuggestionResponse;
import com.stockpulse.dto.SuggestionActionRequest;
import com.stockpulse.service.SuggestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reorder-suggestions")
@RequiredArgsConstructor
public class ReorderSuggestionController {

    private final SuggestionService suggestionService;

    @GetMapping
    public ResponseEntity<List<ReorderSuggestionResponse>> getReorderSuggestions(
            @RequestParam(required = false) SuggestionStatus status) {
        return ResponseEntity.ok(suggestionService.getReorderSuggestions(status));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReorderSuggestionResponse> processAction(
            @PathVariable Long id,
            @Valid @RequestBody SuggestionActionRequest request) {
        return ResponseEntity.ok(suggestionService.processReorderAction(id, request));
    }
}
