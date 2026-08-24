package com.stockpulse.controller;

import com.stockpulse.domain.Product.Category;
import com.stockpulse.dto.*;
import com.stockpulse.service.ProductService;
import com.stockpulse.service.SuggestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SuggestionService suggestionService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(productService.getAllProducts(category, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {
        return ResponseEntity.ok(productService.updateStock(id, request));
    }

    @PostMapping("/{id}/orders")
    public ResponseEntity<ProductResponse> createOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(productService.processOrder(id, request));
    }

    @PostMapping("/{id}/suggest-pricing")
    public ResponseEntity<PricingSuggestionResponse> suggestPricing(@PathVariable Long id) {
        return ResponseEntity.ok(suggestionService.generateManualPricingSuggestion(id));
    }

    @PostMapping("/{id}/suggest-reorder")
    public ResponseEntity<ReorderSuggestionResponse> suggestReorder(@PathVariable Long id) {
        return ResponseEntity.ok(suggestionService.generateManualReorderSuggestion(id));
    }
}
