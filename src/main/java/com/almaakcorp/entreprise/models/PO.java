package com.almaakcorp.entreprise.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "po")
public class PO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long poiId;

    @Column(unique = true)
    private String poNumber;

    private String rfqNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "quotation_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Quotations quotation; // nullable to avoid delete issues

    private Instant receivedAt;

    private Boolean delivered = false;

    private Instant deliveredAt;

    private BigDecimal income;

    private BigDecimal poTotalAmount;

    private Boolean paid = false;

    private Instant paidAt;

    private String filePath; // stored in filesystem

    // Getters and setters
    public Long getPoiId() { return poiId; }
    public void setPoiId(Long poiId) { this.poiId = poiId; }
    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }
    public String getRfqNumber() { return rfqNumber; }
    public void setRfqNumber(String rfqNumber) { this.rfqNumber = rfqNumber; }
    public Quotations getQuotation() { return quotation; }
    public void setQuotation(Quotations quotation) { this.quotation = quotation; }
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
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}
