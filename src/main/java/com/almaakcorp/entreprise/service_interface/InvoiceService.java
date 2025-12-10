package com.almaakcorp.entreprise.service_interface;

import com.almaakcorp.entreprise.dto.InvoiceItemDTO;
import com.almaakcorp.entreprise.models.Invoices;
import com.almaakcorp.entreprise.enums.Status;
import com.almaakcorp.entreprise.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceService {
    
    // Basic CRUD operations
    Invoices saveInvoice(Invoices invoice);
    
    Optional<Invoices> findById(Long id);
    
    Optional<Invoices> findByInvoiceId(String invoiceId);
    
    Page<Invoices> findAll(Pageable pageable);
    
    Invoices updateInvoice(Long id, Invoices invoice);
    
    void deleteInvoice(Long id);
    
    // Search and filter operations
    Page<Invoices> findByStatus(Status status, Pageable pageable);
    
    Page<Invoices> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);
    
    Page<Invoices> findByCustomerName(String customerName, Pageable pageable);
    
    Page<Invoices> findByCustomerEmail(String customerEmail, Pageable pageable);
    
    List<Invoices> findByCustomerId(String customerId);
    
    Optional<Invoices> findByQuotationId(String quotationId);
    
    // Date-based queries
    List<Invoices> findOverdueInvoices();
    
    List<Invoices> findInvoicesDueBetween(LocalDate startDate, LocalDate endDate);
    
    List<Invoices> findInvoicesIssuedBetween(LocalDate startDate, LocalDate endDate);
    
    // Payment operations
    Invoices updatePaymentStatus(Long id, PaymentStatus paymentStatus, String paymentMethod, LocalDate paymentDate);
    
    // Statistics and reporting
    Long countByStatus(Status status);
    
    Long countByPaymentStatus(PaymentStatus paymentStatus);
    
    Double getTotalPaidAmountBetweenDates(LocalDate startDate, LocalDate endDate);
    
    // Utility methods
    String generateNextInvoiceId();
    
    Invoices createInvoiceFromQuotation(String quotationId);
    
    // New methods for handling invoice items
    Invoices createInvoiceWithItems(Invoices invoice, List<InvoiceItemDTO> itemDTOs);
    
    Invoices updateInvoiceWithItems(Long id, Invoices invoice, List<InvoiceItemDTO> itemDTOs);
}