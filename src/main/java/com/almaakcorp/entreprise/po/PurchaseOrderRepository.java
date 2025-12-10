package com.almaakcorp.entreprise.po;

import com.almaakcorp.entreprise.models.PO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface PurchaseOrderRepository extends JpaRepository<PO, Long> {
    long countByStatusIgnoreCase(String status);

    @Query("select coalesce(sum(p.poTotalAmount), 0) from PO p")
    BigDecimal sumTotalAmount();

    // Alternative common naming
    @Query("select coalesce(sum(p.poTotalAmount), 0) from PO p")
    BigDecimal sumAmount();
}
