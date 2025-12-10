package com.almaakcorp.entreprise.models;
import com.almaakcorp.entreprise.enums.Status;
import com.almaakcorp.entreprise.enums.PaymentStatus;
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
@Table(name = "invoices")
public class Invoices {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
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
    
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(length = 1000)
    private String notes;

    @Column(length = 1000)
    private String terms;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> invoiceItems = new ArrayList<>();

    private Double subtotal = 0.0;
    private Double tax = 0.0;
    private Double taxRate = 0.0;
    private Double discount = 0.0;
    private String discountType;
    private Double totalAmount = 0.0;
    
    // Payment related fields
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    private String paymentMethod;
    private LocalDate paymentDate;

    @CreationTimestamp
    private LocalDate createdAt;
    @UpdateTimestamp
    private LocalDate updatedAt;
}

