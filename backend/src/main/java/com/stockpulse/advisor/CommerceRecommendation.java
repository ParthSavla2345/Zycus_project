package com.stockpulse.advisor;

/**
 * Combined result containing both pricing and reorder recommendations.
 * Returned by any CommerceAdvisor implementation.
 */
public record CommerceRecommendation(
        PricingRecommendation pricing,
        ReorderRecommendation reorder
) {}
