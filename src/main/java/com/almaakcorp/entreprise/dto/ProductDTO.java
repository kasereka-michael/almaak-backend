package com.almaakcorp.entreprise.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Data Transfer Object for Products
 * Used for transferring product data between layers without exposing the entity model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    private String productName;
    private String productSKU;
    private String productPartNumber;
    private String productCategory;
    private String productManufacturer;
    private String productDescription;
    private BigDecimal productSellingPrice;
    private BigDecimal productNormalPrice;
    private long productCurrentQuantity;
    private long productMinimumQuantity;
    private String storageLocation;
    private String supplierInfo;
    private String notes;
    private Date createdAt;
    private Date updatedAt;
}