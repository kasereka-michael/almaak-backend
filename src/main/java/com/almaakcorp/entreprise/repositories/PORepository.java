package com.almaakcorp.entreprise.repositories;

import com.almaakcorp.entreprise.models.PO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface PORepository extends JpaRepository<PO, Long> {
    boolean existsByPoNumber(String poNumber);

    long countByPaidIsTrue();

    @Query("select coalesce(sum(p.poTotalAmount), 0) from PO p")
    BigDecimal sumTotalAmount();

    @Query("select coalesce(sum(p.poTotalAmount), 0) from PO p")
    BigDecimal sumAmount();
}
