package com.almaakcorp.entreprise.repositories;

import com.almaakcorp.entreprise.models.PO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface PORepository extends JpaRepository<PO, Long> {
    boolean existsByPoNumber(String poNumber);

    long countByPaidIsTrue();

    @Query("select coalesce(sum(p.poTotalAmount), 0) from PO p")
    BigDecimal sumTotalAmount();

    @Query("select coalesce(sum(p.poTotalAmount), 0) from PO p")
    BigDecimal sumAmount();

    // Reports helpers
    java.util.List<PO> findByReceivedAtBetween(java.time.Instant start, java.time.Instant end);

    long countByPaidIsTrueAndPaidAtBetween(java.time.Instant start, java.time.Instant end);

    @Query("select coalesce(sum(p.income), 0) from PO p where p.receivedAt between :start and :end")
    BigDecimal sumIncomeByReceivedAtBetween(@org.springframework.data.repository.query.Param("start") java.time.Instant start,
                                            @org.springframework.data.repository.query.Param("end") java.time.Instant end);

    @Query("select coalesce(sum(p.poTotalAmount), 0) from PO p where p.paid = true and p.paidAt between :start and :end")
    BigDecimal sumPoTotalAmountByPaidAtBetween(@org.springframework.data.repository.query.Param("start") java.time.Instant start,
                                               @org.springframework.data.repository.query.Param("end") java.time.Instant end);
}
