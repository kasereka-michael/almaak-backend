package com.almaakcorp.entreprise.migration.source;

import com.almaakcorp.entreprise.models.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SourceProductRepository extends JpaRepository<Products, Long> {
    Optional<Products> findByProductId(Long productId);
    Products findByProductName(String productName);
}
