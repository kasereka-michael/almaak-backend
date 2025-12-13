package com.almaakcorp.entreprise.migration.source;

import com.almaakcorp.entreprise.models.PO;
import com.almaakcorp.entreprise.models.POExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourcePOExpenseRepository extends JpaRepository<POExpense, Long> {
    List<POExpense> findByPo(PO spo);
}
