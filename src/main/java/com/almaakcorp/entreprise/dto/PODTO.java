package com.almaakcorp.entreprise.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
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


}
