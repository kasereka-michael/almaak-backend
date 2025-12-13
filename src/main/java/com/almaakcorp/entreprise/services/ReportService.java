package com.almaakcorp.entreprise.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {
    List<Map<String, Object>> getTopQuotedProducts(LocalDate start, LocalDate end, int limit);
    Map<String, Object> getPOsReceived(LocalDate start, LocalDate end);
    Map<String, Object> getPOsPaidSummary(LocalDate start, LocalDate end);
    Map<String, Object> getRevenue(LocalDate start, LocalDate end);
    Map<String, Object> getExpenses(LocalDate start, LocalDate end);
}
