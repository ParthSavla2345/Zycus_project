package com.stockpulse.service;

import com.stockpulse.domain.Product;
import com.stockpulse.dto.OrderRequest;
import com.stockpulse.dto.ProductResponse;
import com.stockpulse.event.StockSignalEvent;
import com.stockpulse.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .sku("PRD-004")
                .name("Slim Fit Chino Pants")
                .category(Product.Category.APPAREL)
                .currentPrice(new BigDecimal("49.99"))
                .stockLevel(25)
                .reorderThreshold(10)
                .demandVelocity(4.5)
                .status(Product.ProductStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("3. Inventory-low detection: Order reduces stock below threshold and publishes INVENTORY_LOW event")
    void testInventoryLowDetectionOnOrder() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findAverageDemandVelocityByCategory(Product.Category.APPAREL)).thenReturn(10.0);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ordering 18 units: 25 - 18 = 7 (threshold is 10)
        OrderRequest request = new OrderRequest(18);
        ProductResponse response = productService.processOrder(1L, request);

        assertEquals(7, response.stockLevel());

        ArgumentCaptor<StockSignalEvent> captor = ArgumentCaptor.forClass(StockSignalEvent.class);
        verify(eventPublisher, atLeastOnce()).publishEvent(captor.capture());

        boolean hasInventoryLow = captor.getAllValues().stream()
                .anyMatch(e -> e.getTriggerReason() == com.stockpulse.domain.TriggerReason.INVENTORY_LOW);
        assertTrue(hasInventoryLow, "Expected INVENTORY_LOW event to be published");
    }

    @Test
    @DisplayName("4. Demand-spike detection: Large order causes demand velocity to exceed 2x category avg")
    void testDemandSpikeDetectionOnOrder() {
        // Category average velocity is 5.0
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findAverageDemandVelocityByCategory(Product.Category.APPAREL)).thenReturn(5.0);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ordering 10 units: velocity increases to 4.5 + (10 * 1.5) = 19.5 (> 2 * 5.0)
        OrderRequest request = new OrderRequest(10);
        ProductResponse response = productService.processOrder(1L, request);

        assertTrue(response.demandVelocity() > 10.0);

        ArgumentCaptor<StockSignalEvent> captor = ArgumentCaptor.forClass(StockSignalEvent.class);
        verify(eventPublisher, atLeastOnce()).publishEvent(captor.capture());

        boolean hasDemandSpike = captor.getAllValues().stream()
                .anyMatch(e -> e.getTriggerReason() == com.stockpulse.domain.TriggerReason.DEMAND_SPIKE);
        assertTrue(hasDemandSpike, "Expected DEMAND_SPIKE event to be published");
    }
}
