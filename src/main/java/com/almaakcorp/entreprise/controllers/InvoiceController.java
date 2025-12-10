package com.almaakcorp.entreprise.controllers;

import com.almaakcorp.entreprise.dto.InvoiceRequestDTO;
import com.almaakcorp.entreprise.models.Invoices;
import com.almaakcorp.entreprise.enums.Status;
import com.almaakcorp.entreprise.enums.PaymentStatus;
import com.almaakcorp.entreprise.service_interface.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/invoice/v1")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    // Get all invoices with pagination
    @GetMapping("/find-all")
    public ResponseEntity<Map<String, Object>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerEmail) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Invoices> invoicePage;
            
            // Apply filters
            if (status != null && !status.isEmpty()) {
                invoicePage = invoiceService.findByStatus(Status.valueOf(status.toUpperCase()), pageable);
            } else if (paymentStatus != null && !paymentStatus.isEmpty()) {
                invoicePage = invoiceService.findByPaymentStatus(PaymentStatus.valueOf(paymentStatus.toUpperCase()), pageable);
            } else if (customerName != null && !customerName.isEmpty()) {
                invoicePage = invoiceService.findByCustomerName(customerName, pageable);
            } else if (customerEmail != null && !customerEmail.isEmpty()) {
                invoicePage = invoiceService.findByCustomerEmail(customerEmail, pageable);
            } else {
                invoicePage = invoiceService.findAll(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("invoice", invoicePage.getContent());
            response.put("currentPage", invoicePage.getNumber());
            response.put("totalItems", invoicePage.getTotalElements());
            response.put("totalPages", invoicePage.getTotalPages());
            response.put("isFirst", invoicePage.isFirst());
            response.put("isLast", invoicePage.isLast());
            response.put("hasNext", invoicePage.hasNext());
            response.put("hasPrevious", invoicePage.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch invoices: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Get invoice by ID
    @GetMapping("/find-by-id/{id}")
    public ResponseEntity<Invoices> getInvoiceById(@PathVariable Long id) {
        try {
            Optional<Invoices> invoice = invoiceService.findById(id);
            return invoice.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get invoice by invoice ID
    @GetMapping("/find/{invoiceId}")
    public ResponseEntity<Invoices> getInvoiceByInvoiceId(@PathVariable String invoiceId) {
        try {
            Optional<Invoices> invoice = invoiceService.findByInvoiceId(invoiceId);
            return invoice.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Create new invoice
    @PostMapping("/save-invoice")
    public ResponseEntity<?> createInvoice(@RequestBody InvoiceRequestDTO invoiceRequestDTO) {
        log.info("REST request to save a new invoice: {}", invoiceRequestDTO.getInvoiceId());
        try {
            // Convert DTO to entity
            Invoices invoice = convertDTOToEntity(invoiceRequestDTO);
            
            // Create invoice with items
            Invoices result = invoiceService.createInvoiceWithItems(invoice, invoiceRequestDTO.getItems());
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error saving invoice: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    Map.of("error", "Failed to save invoice", "message", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
    
    // Update invoice
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateInvoice(@PathVariable Long id, @RequestBody InvoiceRequestDTO invoiceRequestDTO) {
        log.info("REST request to update invoice with ID: {}", id);
        try {
            // Convert DTO to entity
            Invoices invoice = convertDTOToEntity(invoiceRequestDTO);
            
            // Update invoice with items
            Invoices result = invoiceService.updateInvoiceWithItems(id, invoice, invoiceRequestDTO.getItems());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Invoice not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating invoice: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    Map.of("error", "Failed to update invoice", "message", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
    
    // Delete invoice
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteInvoice(@PathVariable Long id) {
        try {
            invoiceService.deleteInvoice(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invoice deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Update payment status
    @PutMapping("/update-payment/{id}")
    public ResponseEntity<Invoices> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> paymentData) {
        try {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(
                paymentData.get("paymentStatus").toString().toUpperCase()
            );
            String paymentMethod = (String) paymentData.get("paymentMethod");
            LocalDate paymentDate = paymentData.get("paymentDate") != null ? 
                LocalDate.parse(paymentData.get("paymentDate").toString()) : null;
            
            Invoices updatedInvoice = invoiceService.updatePaymentStatus(id, paymentStatus, paymentMethod, paymentDate);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Get last invoice ID for generating next ID
    @GetMapping("/get-last-id")
    public ResponseEntity<Map<String, String>> getLastInvoiceId() {
        try {
            String nextId = invoiceService.generateNextInvoiceId();
            Map<String, String> response = new HashMap<>();
            response.put("nextInvoiceId", nextId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Create invoice from quotation
    @PostMapping("/create-from-quotation/{quotationId}")
    public ResponseEntity<Invoices> createInvoiceFromQuotation(@PathVariable String quotationId) {
        try {
            Invoices invoice = invoiceService.createInvoiceFromQuotation(quotationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Get invoices by customer ID
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Invoices>> getInvoicesByCustomerId(@PathVariable String customerId) {
        try {
            List<Invoices> invoices = invoiceService.findByCustomerId(customerId);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get overdue invoices
    @GetMapping("/overdue")
    public ResponseEntity<List<Invoices>> getOverdueInvoices() {
        try {
            List<Invoices> overdueInvoices = invoiceService.findOverdueInvoices();
            return ResponseEntity.ok(overdueInvoices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get invoice statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getInvoiceStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalDraft", invoiceService.countByStatus(Status.DRAFT));
            stats.put("totalSent", invoiceService.countByStatus(Status.SENT));
            stats.put("totalPaid", invoiceService.countByPaymentStatus(PaymentStatus.PAID));
            stats.put("totalUnpaid", invoiceService.countByPaymentStatus(PaymentStatus.UNPAID));
            stats.put("totalOverdue", invoiceService.findOverdueInvoices().size());
            
            // Get total paid amount for current month
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            stats.put("totalPaidThisMonth", invoiceService.getTotalPaidAmountBetweenDates(startOfMonth, endOfMonth));
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Helper method to convert DTO to entity
    private Invoices convertDTOToEntity(InvoiceRequestDTO dto) {
        Invoices invoice = new Invoices();
        invoice.setId(dto.getId());
        invoice.setInvoiceId(dto.getInvoiceId());
        invoice.setQuotationId(dto.getQuotationId());
        invoice.setCustomerId(dto.getCustomerId());
        invoice.setCustomerName(dto.getCustomerName());
        invoice.setCustomerEmail(dto.getCustomerEmail());
        invoice.setCustomerAddress(dto.getCustomerAddress());
        invoice.setReference(dto.getReference());
        invoice.setAttention(dto.getAttention());
        invoice.setIssueDate(dto.getIssueDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setDownloadPath(dto.getDownloadPath());
        invoice.setStatus(dto.getStatus());
        invoice.setNotes(dto.getNotes());
        invoice.setTerms(dto.getTerms());
        invoice.setSubtotal(dto.getSubtotal());
        invoice.setTax(dto.getTax());
        invoice.setTaxRate(dto.getTaxRate());
        invoice.setDiscount(dto.getDiscount());
        invoice.setDiscountType(dto.getDiscountType());
        invoice.setTotalAmount(dto.getTotalAmount());
        invoice.setPaymentStatus(dto.getPaymentStatus());
        invoice.setPaymentMethod(dto.getPaymentMethod());
        invoice.setPaymentDate(dto.getPaymentDate());
        return invoice;
    }
}