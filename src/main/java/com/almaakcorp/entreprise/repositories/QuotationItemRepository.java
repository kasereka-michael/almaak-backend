package com.almaakcorp.entreprise.repositories;

import com.almaakcorp.entreprise.models.QuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuotationItemRepository extends JpaRepository<QuotationItem, Long> {
    
    @Query("SELECT qi FROM QuotationItem qi WHERE qi.quotation.id = :quotationId")
    List<QuotationItem> findByQuotationId(@Param("quotationId") Long quotationId);
    
    @Query("SELECT qi FROM QuotationItem qi WHERE qi.quotation.quotationId = :quotationId")
    List<QuotationItem> findByQuotationQuotationId(@Param("quotationId") String quotationId);
    
    void deleteByQuotationId(Long quotationId);
}