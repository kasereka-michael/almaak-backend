package com.almaakcorp.entreprise.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface QuotationItemRepository extends JpaRepository<com.almaakcorp.entreprise.models.QuotationItem, Long> {

    interface TopQuotedRow {
        Long getProductId();
        String getProductName();
        Long getTimesQuoted();
        Long getTotalQty();
    }

    @Query("""
        select qi.product.id as productId,
               qi.product.name as productName,
               count(qi.id) as timesQuoted,
               coalesce(sum(qi.quantity),0) as totalQty
        from QuotationItem qi
        join qi.quotation q
        where (q.createdAt between :start and :end)
        group by qi.product.id, qi.product.name
        order by timesQuoted desc
        """)
    List<TopQuotedRow> findTopQuotedBetween(@Param("start") LocalDate start,
                                            @Param("end") LocalDate end,
                                            @Param("limit") int limit);
}
