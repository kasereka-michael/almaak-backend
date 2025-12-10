package com.almaakcorp.entreprise.dashboard;

import com.almaakcorp.entreprise.dashboard.dto.DashboardSummaryResponse;
import com.almaakcorp.entreprise.dashboard.dto.DashboardSummaryResponse.Meta;
import com.almaakcorp.entreprise.dashboard.dto.DashboardSummaryResponse.Snapshot;
import com.almaakcorp.entreprise.dashboard.dto.DashboardSummaryResponse.Revenue;
import com.almaakcorp.entreprise.dashboard.dto.DashboardSummaryResponse.Receivables;
import com.almaakcorp.entreprise.dashboard.dto.DashboardSummaryResponse.Quotations;
import com.almaakcorp.entreprise.dashboard.dto.DashboardSummaryResponse.PurchaseOrders;
import com.almaakcorp.entreprise.dashboard.dto.DashboardSummaryResponse.Inventory;
import com.almaakcorp.entreprise.dashboard.dto.DashboardSummaryResponse.Users;
import com.almaakcorp.entreprise.dashboard.dto.DashboardSummaryResponse.ExchangeRate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class DashboardService {

    private final DashboardStatsPort statsPort;

    public DashboardService(DashboardStatsPort statsPort) {
        this.statsPort = statsPort;
    }

    public DashboardSummaryResponse getSummary(String period) {
        // Defaults
        long quotationsTotal = statsPort.countQuotations();
        long quotationsAccepted = statsPort.countAcceptedQuotations();
        long posTotal = statsPort.countPOs();
        long posPaid = statsPort.countPaidPOs();
        BigDecimal poIncome = statsPort.sumPOIncome();

        // Optional/placeholder values
        String currency = statsPort.getBaseCurrency();
        BigDecimal revenueMtd = statsPort.getRevenueMonthToDate();
        BigDecimal openAR = statsPort.getOpenReceivables();
        BigDecimal stockValuation = statsPort.getStockValuation();
        int activeUsers = statsPort.getActiveUsersToday();

        // win rate
        double winRate = 0d;
        if (quotationsTotal > 0) {
            winRate = new BigDecimal(quotationsAccepted)
                    .divide(new BigDecimal(quotationsTotal), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        DashboardSummaryResponse res = new DashboardSummaryResponse();
        res.setMeta(new Meta(currency));
        Snapshot snapshot = new Snapshot();
        snapshot.setRevenue(new Revenue(revenueMtd, currency));
        snapshot.setReceivables(new Receivables(openAR));

        Quotations q = new Quotations();
        q.setTotalCount(quotationsTotal);
        q.setAcceptedCount(quotationsAccepted);
        q.setWinRate(winRate);
        snapshot.setQuotations(q);

        PurchaseOrders p = new PurchaseOrders();
        p.setTotalCount(posTotal);
        p.setPaidCount(posPaid);
        p.setTotalIncome(poIncome);
        snapshot.setPurchaseOrders(p);

        snapshot.setInventory(new Inventory(stockValuation));
        snapshot.setUsers(new Users(activeUsers));
        snapshot.setExchangeRate(new ExchangeRate(currency));

        res.setSnapshot(snapshot);
        return res;
    }
}
