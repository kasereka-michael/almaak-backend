package com.almaakcorp.entreprise.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_notes")
@Getter
@Setter
@RequiredArgsConstructor
public class DeliveryNotes {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private long dnId;
     private String trackingNumber;
     private String customerId;
     private String customerName;
     private String customerEmail;
     private String customerAddress;
     private String department;
     private String purchaseOrder;
     private String requesterName;
     private boolean submitted;
     private String deliveredBy;
     private String receivedBy;
     private String deliveryDatePdfPath;
     @OneToMany(mappedBy = "dn", fetch = FetchType.EAGER, cascade =
             {CascadeType.DETACH, CascadeType.PERSIST,CascadeType.MERGE},orphanRemoval = true)
     private List<Products> items = new ArrayList<>();
     @CreationTimestamp
     private LocalDate createdAt;
     @UpdateTimestamp
     private LocalDate updatedAt;

}
