package com.almaakcorp.entreprise.repositories;

import com.almaakcorp.entreprise.models.POExpense;
import com.almaakcorp.entreprise.models.PO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface POExpenseRepository extends JpaRepository<POExpense, Long> {
    List<POExpense> findByPo(PO po);

    @org.springframework.data.jpa.repository.Query("select coalesce(sum(e.amount),0) from POExpense e join e.po p where p.receivedAt between :start and :end")
    java.math.BigDecimal sumAmountByPoReceivedAtBetween(@org.springframework.data.repository.query.Param("start") java.time.Instant start,
                                                        @org.springframework.data.repository.query.Param("end") java.time.Instant end);
}
