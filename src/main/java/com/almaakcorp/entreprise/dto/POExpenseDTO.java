package com.almaakcorp.entreprise.dto;

import java.math.BigDecimal;

public class POExpenseDTO {
    private Long id;
    private Long poId;
    private String expenseName;
    private BigDecimal amount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPoId() { return poId; }
    public void setPoId(Long poId) { this.poId = poId; }
    public String getExpenseName() { return expenseName; }
    public void setExpenseName(String expenseName) { this.expenseName = expenseName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
