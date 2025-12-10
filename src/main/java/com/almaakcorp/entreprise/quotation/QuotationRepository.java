package com.almaakcorp.entreprise.quotation;

import com.almaakcorp.entreprise.enums.Status;
import com.almaakcorp.entreprise.models.Quotations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuotationRepository extends JpaRepository<Quotations, Long> {
    long countByStatusIgnoreCase(Status status);

    // Some projects use French field names like 'eta'
    @Query("select count(q) from Quotations q where lower(q.eta) = lower(?1)")
    long countByEtaIgnoreCase(String eta);
}
