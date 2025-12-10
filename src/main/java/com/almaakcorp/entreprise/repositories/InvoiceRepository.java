package com.almaakcorp.entreprise.repositories;

import com.almaakcorp.entreprise.models.Invoices;
import com.almaakcorp.entreprise.enums.Status;
import com.almaakcorp.entreprise.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoices, Long> {
    
    Optional<Invoices> findByInvoiceId(String invoiceId);
    
    Page<Invoices> findByStatus(Status status, Pageable pageable);
    
    Page<Invoices> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);
    
    Page<Invoices> findByCustomerNameContainingIgnoreCase(String customerName, Pageable pageable);
    
    Page<Invoices> findByCustomerEmailContainingIgnoreCase(String customerEmail, Pageable pageable);
    
    List<Invoices> findByDueDateBefore(LocalDate date);
    
    List<Invoices> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT i FROM Invoices i WHERE i.issueDate BETWEEN :startDate AND :endDate")
    List<Invoices> findByIssueDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT i FROM Invoices i WHERE i.customerId = :customerId ORDER BY i.createdAt DESC")
    List<Invoices> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") String customerId);
    
    @Query("SELECT i FROM Invoices i WHERE i.quotationId = :quotationId")
    Optional<Invoices> findByQuotationId(@Param("quotationId") String quotationId);
    
    @Query("SELECT COUNT(i) FROM Invoices i WHERE i.status = :status")
    Long countByStatus(@Param("status") Status status);
    
    @Query("SELECT COUNT(i) FROM Invoices i WHERE i.paymentStatus = :paymentStatus")
    Long countByPaymentStatus(@Param("paymentStatus") PaymentStatus paymentStatus);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoices i WHERE i.paymentStatus = 'PAID' AND i.paymentDate BETWEEN :startDate AND :endDate")
    Double getTotalPaidAmountBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT i FROM Invoices i ORDER BY i.id DESC LIMIT 1")
    Optional<Invoices> findLastInvoice();
}