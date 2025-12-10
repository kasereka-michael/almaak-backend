package com.almaakcorp.entreprise.dto;

import com.almaakcorp.entreprise.enums.PaymentStatus;
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
public class InvoiceRequestDTO {
    private Long id;
    private String invoiceId;
    private String quotationId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerAddress;
    private String reference;
    private String attention;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String downloadPath;
    private Status status;
    private String notes;
    private String terms;
    private Double subtotal;
    private Double tax;
    private Double taxRate;
    private Double discount;
    private String discountType;
    private Double totalAmount;
    
    // Payment related fields
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private LocalDate paymentDate;
    
    // List of items in the invoice
    private List<InvoiceItemDTO> items;
}