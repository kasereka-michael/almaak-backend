package com.almaakcorp.entreprise.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class PODTO {
    private Long poiId;
    private String poNumber;
    private String rfqNumber;
    private Long quotationId;
    private Instant receivedAt;
    private Boolean delivered;
    private Instant deliveredAt;
    private BigDecimal income;
    private BigDecimal poTotalAmount;
    private Boolean paid;
    private Instant paidAt;
    private String fileUrl; // public URL to download/view

    public Long getPoiId() { return poiId; }
    public void setPoiId(Long poiId) { this.poiId = poiId; }
    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }
    public String getRfqNumber() { return rfqNumber; }
    public void setRfqNumber(String rfqNumber) { this.rfqNumber = rfqNumber; }
    public Long getQuotationId() { return quotationId; }
    public void setQuotationId(Long quotationId) { this.quotationId = quotationId; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
    public Boolean getDelivered() { return delivered; }
    public void setDelivered(Boolean delivered) { this.delivered = delivered; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
    public BigDecimal getIncome() { return income; }
    public void setIncome(BigDecimal income) { this.income = income; }
    public BigDecimal getPoTotalAmount() { return poTotalAmount; }
    public void setPoTotalAmount(BigDecimal poTotalAmount) { this.poTotalAmount = poTotalAmount; }
    public Boolean getPaid() { return paid; }
    public void setPaid(Boolean paid) { this.paid = paid; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}
