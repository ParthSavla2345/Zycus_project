package com.stockpulse.repository;

import com.stockpulse.domain.Product;
import com.stockpulse.domain.Product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByCategory(Category category);

    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Average demand velocity across all products in a given category.
     * Used for demand spike detection.
     */
    @Query("SELECT AVG(p.demandVelocity) FROM Product p WHERE p.category = :category")
    Double findAverageDemandVelocityByCategory(Category category);
}
