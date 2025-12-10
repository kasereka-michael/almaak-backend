package com.almaakcorp.entreprise.models;

import com.almaakcorp.entreprise.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quotations")
public class Quotations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String quotationId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerAddress;
    private String reference;
    private String attention;
    private LocalDate validUntil;
    private String downloadPath;
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(length = 1000)
    private String notes;

    @Column(length = 1000)
    private String terms;

    @OneToMany(mappedBy = "quotation", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuotationItem> quotationItems = new ArrayList<>();

    private Double subtotal = 0.0;
    private Double tax = 0.0;
    private Double taxRate = 0.0;
    private Double discount = 0.0;

    private String discountType;

    private Double totalAmount = 0.0;
    private Double expectedIncome = 0.0;
    
    private String eta; // Estimated Time of Arrival/Delivery

    @CreationTimestamp
    private LocalDate createdAt;
    @UpdateTimestamp
    private LocalDate updatedAt;
}
