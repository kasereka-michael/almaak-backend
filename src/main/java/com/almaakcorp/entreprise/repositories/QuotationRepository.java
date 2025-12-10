package com.almaakcorp.entreprise.repositories;

import com.almaakcorp.entreprise.enums.Status;
import com.almaakcorp.entreprise.models.Quotations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuotationRepository extends JpaRepository <Quotations, Long> , JpaSpecificationExecutor<Quotations> {
    Quotations findFirstByOrderByQuotationIdDesc();

    Quotations findByQuotationId(String quotationId);

    @Query("SELECT q FROM Quotations q LEFT JOIN FETCH q.quotationItems qi LEFT JOIN FETCH qi.product WHERE q.quotationId = ?1")
    Quotations findByQuotationIdWithItems(String quotationId);

    @Query("SELECT q FROM Quotations q LEFT JOIN FETCH q.quotationItems qi LEFT JOIN FETCH qi.product WHERE q.id = ?1")
    Quotations findByIdWithItems(Long id);

    long countByStatus(Status status);
}
