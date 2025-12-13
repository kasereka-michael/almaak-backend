package com.almaakcorp.entreprise.migration.source;

import com.almaakcorp.entreprise.models.PO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SourcePORepository extends JpaRepository<PO, Long> {
    Optional<PO> findByPoNumber(String poNumber);
}
