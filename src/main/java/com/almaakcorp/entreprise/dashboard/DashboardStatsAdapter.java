package com.almaakcorp.entreprise.dashboard;

import com.almaakcorp.entreprise.enums.PaymentStatus;
import com.almaakcorp.entreprise.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Adapter that should be wired to your repositories. Replace the TODO sections
 * with actual repository queries to your Quotation and Purchase Order entities.
 */
@Service
@RequiredArgsConstructor
public class DashboardStatsAdapter implements DashboardStatsPort {

    private final com.almaakcorp.entreprise.repositories.QuotationRepository quotationRepository;
    private final com.almaakcorp.entreprise.repositories.PORepository purchaseOrderRepository;

    @Override
    public long countQuotations() {
        try {
            return quotationRepository.count();
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public long countAcceptedQuotations() {
        try {
            return quotationRepository.countByStatus(Status.ACCEPTED);
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public long countPOs() {
        try {
            return purchaseOrderRepository.count();
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public long countPaidPOs() {
        try {
            return purchaseOrderRepository.countByPaidIsTrue();
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public BigDecimal sumPOIncome() {
        try {
            BigDecimal sum = purchaseOrderRepository.sumTotalAmount();
            if (sum != null && sum.signum() != 0) return sum;
            BigDecimal sumAlt = purchaseOrderRepository.sumAmount();
            return sumAlt != null ? sumAlt : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public String getBaseCurrency() {
        // TODO: return from settings or company profile
        return "USD";
    }

    @Override
    public BigDecimal getRevenueMonthToDate() {
        // TODO: implement based on invoices/transactions
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getOpenReceivables() {
        // TODO: implement based on AR aging
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getStockValuation() {
        // TODO: implement from inventory valuation
        return BigDecimal.ZERO;
    }

    @Override
    public int getActiveUsersToday() {
        // TODO: implement based on login/activity tracking
        return 0;
    }
}
