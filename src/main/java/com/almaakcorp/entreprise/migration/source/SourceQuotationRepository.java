package com.almaakcorp.entreprise.migration.source;

import com.almaakcorp.entreprise.models.Quotations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SourceQuotationRepository extends JpaRepository<Quotations, Long> {
    Optional<Quotations> findByQuotationId(String quotationId);
}
