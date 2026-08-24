package com.stockpulse.event;

import com.stockpulse.domain.Product;
import com.stockpulse.domain.TriggerReason;
import org.springframework.context.ApplicationEvent;

public class StockSignalEvent extends ApplicationEvent {

    private final Product product;
    private final TriggerReason triggerReason;
    private final double categoryAvgVelocity;

    public StockSignalEvent(Object source, Product product, TriggerReason triggerReason, double categoryAvgVelocity) {
        super(source);
        this.product = product;
        this.triggerReason = triggerReason;
        this.categoryAvgVelocity = categoryAvgVelocity;
    }

    public Product getProduct() { return product; }
    public TriggerReason getTriggerReason() { return triggerReason; }
    public double getCategoryAvgVelocity() { return categoryAvgVelocity; }
}
