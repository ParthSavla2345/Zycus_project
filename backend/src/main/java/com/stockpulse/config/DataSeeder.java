package com.stockpulse.config;

import com.stockpulse.domain.Product;
import com.stockpulse.domain.Product.Category;
import com.stockpulse.domain.Product.ProductStatus;
import com.stockpulse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds 28 demo products on startup if the database is empty or missing initial items.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() >= 20) {
            log.info("Database already seeded with {} products, skipping.", productRepository.count());
            return;
        }

        List<Product> products = List.of(
            // --- Original Core Demo Products (PRD-001 to PRD-008) ---
            Product.builder()
                .sku("PRD-001")
                .name("Wireless Noise-Cancelling Headphones")
                .category(Category.ELECTRONICS)
                .currentPrice(new BigDecimal("149.99"))
                .costPrice(new BigDecimal("85.00"))
                .supplierId("SUP-TECH-01")
                .stockLevel(45)
                .reorderThreshold(20)
                .demandVelocity(3.2)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-002")
                .name("Smart Home Security Camera")
                .category(Category.ELECTRONICS)
                .currentPrice(new BigDecimal("89.99"))
                .costPrice(new BigDecimal("48.00"))
                .supplierId("SUP-TECH-01")
                .stockLevel(30)
                .reorderThreshold(15)
                .demandVelocity(2.8)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-003")
                .name("Organic Cotton T-Shirt")
                .category(Category.APPAREL)
                .currentPrice(new BigDecimal("24.99"))
                .costPrice(new BigDecimal("9.50"))
                .supplierId("SUP-APP-01")
                .stockLevel(8)
                .reorderThreshold(15)
                .demandVelocity(12.0)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-004")
                .name("Slim Fit Chino Pants")
                .category(Category.APPAREL)
                .currentPrice(new BigDecimal("49.99"))
                .costPrice(new BigDecimal("21.00"))
                .supplierId("SUP-APP-01")
                .stockLevel(25)
                .reorderThreshold(10)
                .demandVelocity(4.5)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-005")
                .name("Bamboo Cutting Board Set")
                .category(Category.HOME)
                .currentPrice(new BigDecimal("34.99"))
                .costPrice(new BigDecimal("14.00"))
                .supplierId("SUP-HOME-01")
                .stockLevel(60)
                .reorderThreshold(25)
                .demandVelocity(1.8)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-006")
                .name("Stainless Steel Water Bottle")
                .category(Category.HOME)
                .currentPrice(new BigDecimal("19.99"))
                .costPrice(new BigDecimal("6.50"))
                .supplierId("SUP-HOME-01")
                .stockLevel(80)
                .reorderThreshold(30)
                .demandVelocity(5.1)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-007")
                .name("Bluetooth Portable Speaker")
                .category(Category.ELECTRONICS)
                .currentPrice(new BigDecimal("59.99"))
                .costPrice(new BigDecimal("28.00"))
                .supplierId("SUP-TECH-02")
                .stockLevel(22)
                .reorderThreshold(12)
                .demandVelocity(4.0)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-008")
                .name("Hoodie — Heather Grey")
                .category(Category.APPAREL)
                .currentPrice(new BigDecimal("59.99"))
                .costPrice(new BigDecimal("26.00"))
                .supplierId("SUP-APP-02")
                .stockLevel(11)
                .reorderThreshold(12)
                .demandVelocity(15.0)
                .status(ProductStatus.ACTIVE)
                .build(),

            // --- 20 Additional Demo Products (PRD-009 to PRD-028) ---
            Product.builder()
                .sku("PRD-009")
                .name("Ultra-Wide Curved Gaming Monitor 34\"")
                .category(Category.ELECTRONICS)
                .currentPrice(new BigDecimal("499.99"))
                .costPrice(new BigDecimal("310.00"))
                .supplierId("SUP-TECH-03")
                .stockLevel(14)
                .reorderThreshold(10)
                .demandVelocity(2.1)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-010")
                .name("Ergonomic Mechanical Keyboard RGB")
                .category(Category.ELECTRONICS)
                .currentPrice(new BigDecimal("119.99"))
                .costPrice(new BigDecimal("62.00"))
                .supplierId("SUP-TECH-02")
                .stockLevel(18)
                .reorderThreshold(15)
                .demandVelocity(8.5)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-011")
                .name("Wireless Charging Dock 3-in-1")
                .category(Category.ELECTRONICS)
                .currentPrice(new BigDecimal("39.99"))
                .costPrice(new BigDecimal("16.00"))
                .supplierId("SUP-TECH-01")
                .stockLevel(7)
                .reorderThreshold(20)
                .demandVelocity(6.3)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-012")
                .name("4K Action Sports Camera")
                .category(Category.ELECTRONICS)
                .currentPrice(new BigDecimal("229.99"))
                .costPrice(new BigDecimal("130.00"))
                .supplierId("SUP-TECH-03")
                .stockLevel(12)
                .reorderThreshold(8)
                .demandVelocity(1.5)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-013")
                .name("Noise-Isolating USB Condenser Microphone")
                .category(Category.ELECTRONICS)
                .currentPrice(new BigDecimal("79.99"))
                .costPrice(new BigDecimal("38.00"))
                .supplierId("SUP-TECH-02")
                .stockLevel(28)
                .reorderThreshold(12)
                .demandVelocity(3.9)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-014")
                .name("Smart Fitness Band & Heart Monitor")
                .category(Category.ELECTRONICS)
                .currentPrice(new BigDecimal("49.99"))
                .costPrice(new BigDecimal("22.00"))
                .supplierId("SUP-TECH-01")
                .stockLevel(9)
                .reorderThreshold(15)
                .demandVelocity(7.4)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-015")
                .name("USB-C GaN Fast Charger 100W")
                .category(Category.ELECTRONICS)
                .currentPrice(new BigDecimal("44.99"))
                .costPrice(new BigDecimal("18.50"))
                .supplierId("SUP-TECH-01")
                .stockLevel(55)
                .reorderThreshold(25)
                .demandVelocity(9.0)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-016")
                .name("Merino Wool Crewneck Sweater")
                .category(Category.APPAREL)
                .currentPrice(new BigDecimal("89.99"))
                .costPrice(new BigDecimal("42.00"))
                .supplierId("SUP-APP-03")
                .stockLevel(16)
                .reorderThreshold(12)
                .demandVelocity(3.1)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-017")
                .name("Waterproof All-Weather Windbreaker")
                .category(Category.APPAREL)
                .currentPrice(new BigDecimal("74.99"))
                .costPrice(new BigDecimal("33.00"))
                .supplierId("SUP-APP-02")
                .stockLevel(5)
                .reorderThreshold(14)
                .demandVelocity(11.2)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-018")
                .name("Comfort-Stretch Denim Jeans")
                .category(Category.APPAREL)
                .currentPrice(new BigDecimal("64.99"))
                .costPrice(new BigDecimal("27.00"))
                .supplierId("SUP-APP-01")
                .stockLevel(34)
                .reorderThreshold(15)
                .demandVelocity(4.8)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-019")
                .name("Breathable Athletic Running Shorts")
                .category(Category.APPAREL)
                .currentPrice(new BigDecimal("29.99"))
                .costPrice(new BigDecimal("11.00"))
                .supplierId("SUP-APP-01")
                .stockLevel(42)
                .reorderThreshold(20)
                .demandVelocity(5.6)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-020")
                .name("Thermal Fleece-Lined Joggers")
                .category(Category.APPAREL)
                .currentPrice(new BigDecimal("54.99"))
                .costPrice(new BigDecimal("22.50"))
                .supplierId("SUP-APP-02")
                .stockLevel(6)
                .reorderThreshold(12)
                .demandVelocity(8.0)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-021")
                .name("Classic Pique Cotton Polo Shirt")
                .category(Category.APPAREL)
                .currentPrice(new BigDecimal("34.99"))
                .costPrice(new BigDecimal("13.00"))
                .supplierId("SUP-APP-01")
                .stockLevel(50)
                .reorderThreshold(18)
                .demandVelocity(4.2)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-022")
                .name("Cast Iron Skillet 12-Inch")
                .category(Category.HOME)
                .currentPrice(new BigDecimal("44.99"))
                .costPrice(new BigDecimal("19.00"))
                .supplierId("SUP-HOME-02")
                .stockLevel(22)
                .reorderThreshold(10)
                .demandVelocity(2.4)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-023")
                .name("Ceramic Pour-Over Coffee Maker")
                .category(Category.HOME)
                .currentPrice(new BigDecimal("28.99"))
                .costPrice(new BigDecimal("11.50"))
                .supplierId("SUP-HOME-01")
                .stockLevel(15)
                .reorderThreshold(12)
                .demandVelocity(3.8)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-024")
                .name("Egyptian Cotton Luxury Bath Towel Set")
                .category(Category.HOME)
                .currentPrice(new BigDecimal("49.99"))
                .costPrice(new BigDecimal("21.00"))
                .supplierId("SUP-HOME-03")
                .stockLevel(38)
                .reorderThreshold(15)
                .demandVelocity(4.1)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-025")
                .name("Aroma Essential Oil Ultrasonic Diffuser")
                .category(Category.HOME)
                .currentPrice(new BigDecimal("32.99"))
                .costPrice(new BigDecimal("12.00"))
                .supplierId("SUP-HOME-01")
                .stockLevel(4)
                .reorderThreshold(15)
                .demandVelocity(7.9)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-026")
                .name("Memory Foam Ergonomic Bed Pillow")
                .category(Category.HOME)
                .currentPrice(new BigDecimal("39.99"))
                .costPrice(new BigDecimal("16.00"))
                .supplierId("SUP-HOME-03")
                .stockLevel(27)
                .reorderThreshold(12)
                .demandVelocity(3.0)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-027")
                .name("Airtight Glass Food Storage Containers (10-Pk)")
                .category(Category.HOME)
                .currentPrice(new BigDecimal("42.99"))
                .costPrice(new BigDecimal("18.00"))
                .supplierId("SUP-HOME-02")
                .stockLevel(48)
                .reorderThreshold(20)
                .demandVelocity(6.0)
                .status(ProductStatus.ACTIVE)
                .build(),

            Product.builder()
                .sku("PRD-028")
                .name("Smart LED Desk Lamp with Wireless Charging")
                .category(Category.HOME)
                .currentPrice(new BigDecimal("54.99"))
                .costPrice(new BigDecimal("24.00"))
                .supplierId("SUP-HOME-01")
                .stockLevel(19)
                .reorderThreshold(10)
                .demandVelocity(2.7)
                .status(ProductStatus.ACTIVE)
                .build()
        );

        for (Product product : products) {
            if (productRepository.findBySku(product.getSku()).isEmpty()) {
                productRepository.save(product);
            }
        }
        log.info("Seeding complete. Total catalog count: {}", productRepository.count());
    }
}
