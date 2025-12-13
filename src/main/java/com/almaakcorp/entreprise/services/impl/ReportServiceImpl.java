package com.almaakcorp.entreprise.services.impl;

import com.almaakcorp.entreprise.repositories.POExpenseRepository;
import com.almaakcorp.entreprise.repositories.PORepository;
import com.almaakcorp.entreprise.repositories.QuotationItemRepository;
import com.almaakcorp.entreprise.repositories.QuotationRepository;
import com.almaakcorp.entreprise.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final QuotationItemRepository quotationItemRepository;
    private final QuotationRepository quotationRepository;
    private final PORepository poRepository;
    private final POExpenseRepository poExpenseRepository;

    private Instant startOfDay(LocalDate d) { return d.atStartOfDay().toInstant(ZoneOffset.UTC); }
    private Instant endOfDay(LocalDate d) { return d.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusMillis(1); }

    @Override
    public List<Map<String, Object>> getTopQuotedProducts(LocalDate start, LocalDate end, int limit) {
        var rows = quotationItemRepository.findTopQuotedBetween(start, end);
        return rows.stream().limit(Math.max(1, limit)).map(r -> {
            Map<String,Object> m = new HashMap<>();
            m.put("productId", r.getProductId());
            m.put("productName", r.getProductName());
            m.put("timesQuoted", r.getTimesQuoted());
            m.put("totalQuotedQty", r.getTotalQty());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getPOsReceived(LocalDate start, LocalDate end) {
        Instant s = startOfDay(start); Instant e = endOfDay(end);
        var list = poRepository.findByReceivedAtBetween(s, e).stream().map(po -> {
            Map<String,Object> m = new HashMap<>();
            m.put("poNumber", po.getPoNumber());
            m.put("rfqNumber", po.getRfqNumber());
            m.put("receivedAt", po.getReceivedAt());
            m.put("income", po.getIncome());
            m.put("paid", po.getPaid());
            return m;
        }).collect(Collectors.toList());
        BigDecimal totalIncome = poRepository.sumIncomeByReceivedAtBetween(s, e);
        Map<String,Object> res = new HashMap<>();
        res.put("list", list);
        Map<String,Object> totals = new HashMap<>();
        totals.put("income", totalIncome != null ? totalIncome : BigDecimal.ZERO);
        res.put("totals", totals);
        return res;
    }

    @Override
    public Map<String, Object> getPOsPaidSummary(LocalDate start, LocalDate end) {
        Instant s = startOfDay(start); Instant e = endOfDay(end);
        long countPaid = poRepository.countByPaidIsTrueAndPaidAtBetween(s, e);
        BigDecimal totalIncomePaid = poRepository.sumPoTotalAmountByPaidAtBetween(s, e);
        Map<String,Object> res = new HashMap<>();
        res.put("countPaid", countPaid);
        res.put("totalIncomePaid", totalIncomePaid != null ? totalIncomePaid : BigDecimal.ZERO);
        return res;
    }

    @Override
    public Map<String, Object> getRevenue(LocalDate start, LocalDate end) {
        Instant s = startOfDay(start); Instant e = endOfDay(end);
        BigDecimal revenue = poRepository.sumIncomeByReceivedAtBetween(s, e);
        Map<String,Object> res = new HashMap<>();
        res.put("totalRevenue", revenue != null ? revenue : BigDecimal.ZERO);
        return res;
    }

    @Override
    public Map<String, Object> getExpenses(LocalDate start, LocalDate end) {
        Instant s = startOfDay(start); Instant e = endOfDay(end);
        BigDecimal exp;
        try {
            exp = poExpenseRepository.sumAmountByCreatedAtBetween(s, e);
        } catch (Exception ignored) {
            exp = null;
        }
        Map<String,Object> res = new HashMap<>();
        res.put("totalExpenses", exp != null ? exp : BigDecimal.ZERO);
        return res;
    }

    @Override
    public Map<String, Object> getQuotations(LocalDate start, LocalDate end) {
        var quotes = quotationRepository.findByCreatedAtBetween(start, end);
        var list = quotes.stream().map(q -> {
            Map<String,Object> m = new HashMap<>();
            m.put("id", q.getId());
            m.put("quotationId", q.getQuotationId());
            m.put("customerName", q.getCustomerName());
            m.put("status", q.getStatus() != null ? q.getStatus().toString() : null);
            m.put("totalAmount", q.getTotalAmount());
            m.put("createdAt", q.getCreatedAt());
            return m;
        }).collect(java.util.stream.Collectors.toList());
        BigDecimal total = list.stream()
                .map(m -> {
                    Object v = m.get("totalAmount");
                    try { return new BigDecimal(String.valueOf(v == null ? 0 : v)); } catch (Exception e) { return BigDecimal.ZERO; }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String,Object> res = new HashMap<>();
        res.put("list", list);
        Map<String,Object> totals = new HashMap<>();
        totals.put("amount", total);
        res.put("totals", totals);
        return res;
    }
}
