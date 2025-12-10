package com.almaakcorp.entreprise.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.Date;


@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Table(name = "products")
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false,unique = true)
    private String productName;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "product_sku", unique = true)
    private String productSKU;

    @Column(name = "product_image")
    private String productImage;

    @Column(name = "product_manufacturer")
    private String productManufacturer;

    @Column(name = "product_part_number")
    private String productPartNumber;

    @Column(name = "product_selling_price", precision = 10, scale = 2)
    private BigDecimal productSellingPrice;

    @Column(name = "product_cost_price", precision = 10, scale = 2)
    private BigDecimal productCostPrice;

    @Column(name = "product_normal_price", precision = 10, scale = 2)
    private BigDecimal productNormalPrice;

    @Column(name = "product_current_quantity")
    private Long productCurrentQuantity;

    @Column(name = "product_minimum_quantity")
    private Long productMinimumQuantity;

    @Column(name = "storage_location")
    private String storageLocation;

    @Column(name = "product_supplier_info")
    private String productSupplierInfo;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "product_category")
    private String productCategory;

    @Column(name = "product_status")
    private String productStatus;

    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;






    @ManyToOne
    @JoinColumn(name = "dn_id")
    @JsonIgnore
    private DeliveryNotes dn;


}
