package com.almaakcorp.entreprise.dashboard;

import java.math.BigDecimal;

public interface DashboardStatsPort {
    long countQuotations();
    long countAcceptedQuotations();

    long countPOs();
    long countPaidPOs();
    BigDecimal sumPOIncome();

    String getBaseCurrency();
    BigDecimal getRevenueMonthToDate();
    BigDecimal getOpenReceivables();
    BigDecimal getStockValuation();
    int getActiveUsersToday();
}
