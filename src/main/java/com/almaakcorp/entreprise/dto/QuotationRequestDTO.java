package com.almaakcorp.entreprise.dto;

import com.almaakcorp.entreprise.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationRequestDTO {
    private Long id;
    private String quotationId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerAddress;
    private String reference;
    private String attention;
    private LocalDate validUntil;
    private String downloadPath;
    private Status status;
    private String notes;
    private String terms;
    private String eta;
    private Double subtotal;
    private Double tax;
    private Double taxRate;
    private Double discount;
    private String discountType;
    private Double totalAmount;
    private Double expectedIncome;
    
    // List of items in the quotation
    private List<QuotationItemDTO> items;
}