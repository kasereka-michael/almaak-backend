package com.almaakcorp.entreprise.repositories;

import com.almaakcorp.entreprise.models.POExpense;
import com.almaakcorp.entreprise.models.PO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface POExpenseRepository extends JpaRepository<POExpense, Long> {
    List<POExpense> findByPo(PO po);
}
