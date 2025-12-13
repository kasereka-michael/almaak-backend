package com.almaakcorp.entreprise.migration.source;

import com.almaakcorp.entreprise.models.QuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceQuotationItemRepository extends JpaRepository<QuotationItem, Long> {}
