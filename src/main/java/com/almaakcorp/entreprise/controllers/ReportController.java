package com.almaakcorp.entreprise.controllers;

import com.almaakcorp.entreprise.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report/v1")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/top-quoted-products")
    public List<Map<String, Object>> topQuotedProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return reportService.getTopQuotedProducts(start, end, limit);
    }

    @GetMapping("/po/received")
    public Map<String, Object> poReceived(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return reportService.getPOsReceived(start, end);
    }

    @GetMapping("/po/paid/summary")
    public Map<String, Object> poPaidSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return reportService.getPOsPaidSummary(start, end);
    }

    @GetMapping("/revenue")
    public Map<String, Object> revenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return reportService.getRevenue(start, end);
    }

    @GetMapping("/expenses")
    public Map<String, Object> expenses(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return reportService.getExpenses(start, end);
    }

    @GetMapping("/quotations")
    public Map<String, Object> quotations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return reportService.getQuotations(start, end);
    }
}
