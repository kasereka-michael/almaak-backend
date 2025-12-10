package com.almaakcorp.entreprise.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productDescription;
    private String productSKU;
    private String productPartNumber;
    private String productManufacturer;
    private String productCategory;
    private String productImage;
    private Long quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}