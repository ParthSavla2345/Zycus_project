package com.stockpulse.advisor;

import com.stockpulse.domain.Product;
import com.stockpulse.domain.TriggerReason;

public interface CommerceAdvisor {

    CommerceRecommendation recommend(Product product, TriggerReason triggerReason, double categoryAvgVelocity);
}
