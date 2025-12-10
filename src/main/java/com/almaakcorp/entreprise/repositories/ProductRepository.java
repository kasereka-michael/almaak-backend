package com.almaakcorp.entreprise.repositories;

import com.almaakcorp.entreprise.models.Products;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Products, Long>, JpaSpecificationExecutor<Products> {

    Products findByProductPartNumber(String productPartNumber);

    Optional<Products> findByProductId(Long productId);

    @Query("SELECT p FROM Products p WHERE LOWER(CONCAT(COALESCE(p.productName, ''), COALESCE(p.productPartNumber, ''), COALESCE(p.storageLocation, ''), COALESCE(p.productDescription, ''))) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Products> searchByName(@Param("query") String query, Pageable pageable);
}
