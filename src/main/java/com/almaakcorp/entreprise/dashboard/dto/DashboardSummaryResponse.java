package com.almaakcorp.entreprise.dashboard.dto;

import java.math.BigDecimal;

public class DashboardSummaryResponse {

    private Meta meta;
    private Snapshot snapshot;

    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }

    public Snapshot getSnapshot() { return snapshot; }
    public void setSnapshot(Snapshot snapshot) { this.snapshot = snapshot; }

    // Nested DTOs
    public static class Meta {
        private String currency;
        public Meta() {}
        public Meta(String currency) { this.currency = currency; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }

    public static class Snapshot {
        private Revenue revenue;
        private Receivables receivables;
        private Quotations quotations;
        private PurchaseOrders purchaseOrders;
        private Inventory inventory;
        private Users users;
        private ExchangeRate exchangeRate;

        public Revenue getRevenue() { return revenue; }
        public void setRevenue(Revenue revenue) { this.revenue = revenue; }

        public Receivables getReceivables() { return receivables; }
        public void setReceivables(Receivables receivables) { this.receivables = receivables; }

        public Quotations getQuotations() { return quotations; }
        public void setQuotations(Quotations quotations) { this.quotations = quotations; }

        public PurchaseOrders getPurchaseOrders() { return purchaseOrders; }
        public void setPurchaseOrders(PurchaseOrders purchaseOrders) { this.purchaseOrders = purchaseOrders; }

        public Inventory getInventory() { return inventory; }
        public void setInventory(Inventory inventory) { this.inventory = inventory; }

        public Users getUsers() { return users; }
        public void setUsers(Users users) { this.users = users; }

        public ExchangeRate getExchangeRate() { return exchangeRate; }
        public void setExchangeRate(ExchangeRate exchangeRate) { this.exchangeRate = exchangeRate; }
    }

    public static class Revenue {
        private BigDecimal monthToDate = BigDecimal.ZERO;
        private String currency;
        public Revenue() {}
        public Revenue(BigDecimal monthToDate, String currency) { this.monthToDate = monthToDate; this.currency = currency; }
        public BigDecimal getMonthToDate() { return monthToDate; }
        public void setMonthToDate(BigDecimal monthToDate) { this.monthToDate = monthToDate; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }

    public static class Receivables {
        private BigDecimal openAmount = BigDecimal.ZERO;
        public Receivables() {}
        public Receivables(BigDecimal openAmount) { this.openAmount = openAmount; }
        public BigDecimal getOpenAmount() { return openAmount; }
        public void setOpenAmount(BigDecimal openAmount) { this.openAmount = openAmount; }
    }

    public static class Quotations {
        private long totalCount;
        private long acceptedCount;
        private double winRate;
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
        public long getAcceptedCount() { return acceptedCount; }
        public void setAcceptedCount(long acceptedCount) { this.acceptedCount = acceptedCount; }
        public double getWinRate() { return winRate; }
        public void setWinRate(double winRate) { this.winRate = winRate; }
    }

    public static class PurchaseOrders {
        private long totalCount;
        private long openCount; // optional
        private long paidCount;
        private BigDecimal totalIncome = BigDecimal.ZERO;
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
        public long getOpenCount() { return openCount; }
        public void setOpenCount(long openCount) { this.openCount = openCount; }
        public long getPaidCount() { return paidCount; }
        public void setPaidCount(long paidCount) { this.paidCount = paidCount; }
        public BigDecimal getTotalIncome() { return totalIncome; }
        public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    }

    public static class Inventory {
        private BigDecimal stockValuation = BigDecimal.ZERO;
        public Inventory() {}
        public Inventory(BigDecimal stockValuation) { this.stockValuation = stockValuation; }
        public BigDecimal getStockValuation() { return stockValuation; }
        public void setStockValuation(BigDecimal stockValuation) { this.stockValuation = stockValuation; }
    }

    public static class Users {
        private int activeToday;
        public Users() {}
        public Users(int activeToday) { this.activeToday = activeToday; }
        public int getActiveToday() { return activeToday; }
        public void setActiveToday(int activeToday) { this.activeToday = activeToday; }
    }

    public static class ExchangeRate {
        private String base;
        public ExchangeRate() {}
        public ExchangeRate(String base) { this.base = base; }
        public String getBase() { return base; }
        public void setBase(String base) { this.base = base; }
    }
}
