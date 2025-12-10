package com.almaakcorp.entreprise.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "po_expense")
public class POExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "po_id")
    private PO po;

    private String expenseName;

    private BigDecimal amount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PO getPo() { return po; }
    public void setPo(PO po) { this.po = po; }
    public String getExpenseName() { return expenseName; }
    public void setExpenseName(String expenseName) { this.expenseName = expenseName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
