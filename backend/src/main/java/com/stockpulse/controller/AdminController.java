package com.stockpulse.controller;

import com.stockpulse.config.StrategyConfig;
import com.stockpulse.dto.StrategyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/strategy")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final StrategyConfig strategyConfig;

    @GetMapping
    public ResponseEntity<StrategyResponse> getStrategy() {
        return ResponseEntity.ok(new StrategyResponse(strategyConfig.getStrategy()));
    }

    @PutMapping
    public ResponseEntity<StrategyResponse> setStrategy(@Valid @RequestBody StrategyResponse request) {
        if (request.strategy() == null) {
            return ResponseEntity.badRequest().build();
        }
        strategyConfig.setStrategy(request.strategy());
        log.info("Runtime strategy switched to: {}", request.strategy());
        return ResponseEntity.ok(new StrategyResponse(strategyConfig.getStrategy()));
    }
}
