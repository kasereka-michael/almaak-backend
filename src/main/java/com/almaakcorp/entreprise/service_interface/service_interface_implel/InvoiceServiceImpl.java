package com.almaakcorp.entreprise.service_interface.service_interface_implel;

import com.almaakcorp.entreprise.dto.InvoiceItemDTO;
import com.almaakcorp.entreprise.models.InvoiceItem;
import com.almaakcorp.entreprise.models.Invoices;
import com.almaakcorp.entreprise.models.Products;
import com.almaakcorp.entreprise.models.Quotations;
import com.almaakcorp.entreprise.enums.Status;
import com.almaakcorp.entreprise.enums.PaymentStatus;
import com.almaakcorp.entreprise.repositories.InvoiceRepository;
import com.almaakcorp.entreprise.repositories.ProductRepository;
import com.almaakcorp.entreprise.repositories.QuotationRepository;
import com.almaakcorp.entreprise.service_interface.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceServiceImpl implements InvoiceService {
    

    private final InvoiceRepository invoiceRepository;
    private final QuotationRepository quotationRepository;
    private final ProductRepository productRepository;
    
    @Override
    public Invoices saveInvoice(Invoices invoice) {
        if (invoice.getInvoiceId() == null || invoice.getInvoiceId().isEmpty()) {
            invoice.setInvoiceId(generateNextInvoiceId());
        }
        if (invoice.getIssueDate() == null) {
            invoice.setIssueDate(LocalDate.now());
        }
        if (invoice.getStatus() == null) {
            invoice.setStatus(Status.DRAFT);
        }
        if (invoice.getPaymentStatus() == null) {
            invoice.setPaymentStatus(PaymentStatus.UNPAID);
        }
        return invoiceRepository.save(invoice);
    }
    
    @Override
    public Optional<Invoices> findById(Long id) {
        return invoiceRepository.findById(id);
    }
    
    @Override
    public Optional<Invoices> findByInvoiceId(String invoiceId) {
        return invoiceRepository.findByInvoiceId(invoiceId);
    }
    
    @Override
    public Page<Invoices> findAll(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }
    
    @Override
    public Invoices updateInvoice(Long id, Invoices invoice) {
        Optional<Invoices> existingInvoice = invoiceRepository.findById(id);
        if (existingInvoice.isPresent()) {
            Invoices invoiceToUpdate = existingInvoice.get();
            
            // Update fields
            invoiceToUpdate.setCustomerId(invoice.getCustomerId());
            invoiceToUpdate.setCustomerName(invoice.getCustomerName());
            invoiceToUpdate.setCustomerEmail(invoice.getCustomerEmail());
            invoiceToUpdate.setCustomerAddress(invoice.getCustomerAddress());
            invoiceToUpdate.setReference(invoice.getReference());
            invoiceToUpdate.setAttention(invoice.getAttention());
            invoiceToUpdate.setIssueDate(invoice.getIssueDate());
            invoiceToUpdate.setDueDate(invoice.getDueDate());
            invoiceToUpdate.setStatus(invoice.getStatus());
            invoiceToUpdate.setNotes(invoice.getNotes());
            invoiceToUpdate.setTerms(invoice.getTerms());
            invoiceToUpdate.setInvoiceItems(invoice.getInvoiceItems());
            invoiceToUpdate.setSubtotal(invoice.getSubtotal());
            invoiceToUpdate.setTax(invoice.getTax());
            invoiceToUpdate.setTaxRate(invoice.getTaxRate());
            invoiceToUpdate.setDiscount(invoice.getDiscount());
            invoiceToUpdate.setDiscountType(invoice.getDiscountType());
            invoiceToUpdate.setTotalAmount(invoice.getTotalAmount());
            
            return invoiceRepository.save(invoiceToUpdate);
        }
        throw new RuntimeException("Invoice not found with id: " + id);
    }
    
    @Override
    public void deleteInvoice(Long id) {
        if (invoiceRepository.existsById(id)) {
            invoiceRepository.deleteById(id);
        } else {
            throw new RuntimeException("Invoice not found with id: " + id);
        }
    }
    
    @Override
    public Page<Invoices> findByStatus(Status status, Pageable pageable) {
        return invoiceRepository.findByStatus(status, pageable);
    }
    
    @Override
    public Page<Invoices> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable) {
        return invoiceRepository.findByPaymentStatus(paymentStatus, pageable);
    }
    
    @Override
    public Page<Invoices> findByCustomerName(String customerName, Pageable pageable) {
        return invoiceRepository.findByCustomerNameContainingIgnoreCase(customerName, pageable);
    }
    
    @Override
    public Page<Invoices> findByCustomerEmail(String customerEmail, Pageable pageable) {
        return invoiceRepository.findByCustomerEmailContainingIgnoreCase(customerEmail, pageable);
    }
    
    @Override
    public List<Invoices> findByCustomerId(String customerId) {
        return invoiceRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
    
    @Override
    public Optional<Invoices> findByQuotationId(String quotationId) {
        return invoiceRepository.findByQuotationId(quotationId);
    }
    
    @Override
    public List<Invoices> findOverdueInvoices() {
        return invoiceRepository.findByDueDateBefore(LocalDate.now());
    }
    
    @Override
    public List<Invoices> findInvoicesDueBetween(LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findByDueDateBetween(startDate, endDate);
    }
    
    @Override
    public List<Invoices> findInvoicesIssuedBetween(LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findByIssueDateBetween(startDate, endDate);
    }
    
    @Override
    public Invoices updatePaymentStatus(Long id, PaymentStatus paymentStatus, String paymentMethod, LocalDate paymentDate) {
        Optional<Invoices> existingInvoice = invoiceRepository.findById(id);
        if (existingInvoice.isPresent()) {
            Invoices invoice = existingInvoice.get();
            invoice.setPaymentStatus(paymentStatus);
            invoice.setPaymentMethod(paymentMethod);
            invoice.setPaymentDate(paymentDate);
            
            // If marked as paid, update the payment date to now if not provided
            if (paymentStatus == PaymentStatus.PAID && paymentDate == null) {
                invoice.setPaymentDate(LocalDate.now());
            }
            
            return invoiceRepository.save(invoice);
        }
        throw new RuntimeException("Invoice not found with id: " + id);
    }
    
    @Override
    public Long countByStatus(Status status) {
        return invoiceRepository.countByStatus(status);
    }
    
    @Override
    public Long countByPaymentStatus(PaymentStatus paymentStatus) {
        return invoiceRepository.countByPaymentStatus(paymentStatus);
    }
    
    @Override
    public Double getTotalPaidAmountBetweenDates(LocalDate startDate, LocalDate endDate) {
        Double total = invoiceRepository.getTotalPaidAmountBetweenDates(startDate, endDate);
        return total != null ? total : 0.0;
    }
    
    @Override
    public String generateNextInvoiceId() {
        Optional<Invoices> lastInvoice = invoiceRepository.findLastInvoice();
        if (lastInvoice.isPresent()) {
            String lastId = lastInvoice.get().getInvoiceId();
            if (lastId != null && lastId.startsWith("INV-")) {
                try {
                    int lastNumber = Integer.parseInt(lastId.substring(4));
                    return String.format("INV-%04d", lastNumber + 1);
                } catch (NumberFormatException e) {
                    // If parsing fails, start from 1
                }
            }
        }
        return "INV-0001";
    }
    
    @Override
    public Invoices createInvoiceFromQuotation(String quotationId) {
        Optional<Quotations> quotationOpt = Optional.ofNullable(quotationRepository.findByQuotationId(quotationId));
        if (quotationOpt.isPresent()) {
            Quotations quotation = quotationOpt.get();
            
            Invoices invoice = new Invoices();
            invoice.setInvoiceId(generateNextInvoiceId());
            invoice.setQuotationId(quotation.getQuotationId());
            invoice.setCustomerId(quotation.getCustomerId());
            invoice.setCustomerName(quotation.getCustomerName());
            invoice.setCustomerEmail(quotation.getCustomerEmail());
            invoice.setCustomerAddress(quotation.getCustomerAddress());
            invoice.setReference(quotation.getReference());
            invoice.setAttention(quotation.getAttention());
            invoice.setIssueDate(LocalDate.now());
            invoice.setDueDate(LocalDate.now().plusDays(30));
            invoice.setStatus(Status.DRAFT);
            invoice.setNotes(quotation.getNotes());
            invoice.setTerms(quotation.getTerms());
            invoice.setSubtotal(quotation.getSubtotal());
            invoice.setTax(quotation.getTax());
            invoice.setTaxRate(quotation.getTaxRate());
            invoice.setDiscount(quotation.getDiscount());
            invoice.setDiscountType(quotation.getDiscountType());
            invoice.setTotalAmount(quotation.getTotalAmount());
            invoice.setPaymentStatus(PaymentStatus.UNPAID);
            
            return saveInvoice(invoice);
        }
        throw new RuntimeException("Quotation not found with id: " + quotationId);
    }
    
    @Override
    public Invoices createInvoiceWithItems(Invoices invoice, List<InvoiceItemDTO> itemDTOs) {
        log.info("Creating new invoice with items from DTOs");
        
        // Set default values if not provided
        if (invoice.getInvoiceId() == null || invoice.getInvoiceId().isEmpty()) {
            invoice.setInvoiceId(generateNextInvoiceId());
        }
        if (invoice.getIssueDate() == null) {
            invoice.setIssueDate(LocalDate.now());
        }
        if (invoice.getStatus() == null) {
            invoice.setStatus(Status.DRAFT);
        }
        if (invoice.getPaymentStatus() == null) {
            invoice.setPaymentStatus(PaymentStatus.UNPAID);
        }
        
        // Save the invoice first
        Invoices savedInvoice = invoiceRepository.save(invoice);
        
        // Convert DTOs to InvoiceItem entities and associate with the invoice
        List<InvoiceItem> invoiceItems = convertDTOsToInvoiceItems(itemDTOs, savedInvoice);
        savedInvoice.setInvoiceItems(invoiceItems);
        
        // Save again to persist the items
        return invoiceRepository.save(savedInvoice);
    }
    
    @Override
    public Invoices updateInvoiceWithItems(Long id, Invoices invoice, List<InvoiceItemDTO> itemDTOs) {
        log.info("Updating invoice with ID: {} with new items", id);
        
        Invoices existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
        
        // Update invoice fields
        existingInvoice.setCustomerId(invoice.getCustomerId());
        existingInvoice.setCustomerName(invoice.getCustomerName());
        existingInvoice.setCustomerEmail(invoice.getCustomerEmail());
        existingInvoice.setCustomerAddress(invoice.getCustomerAddress());
        existingInvoice.setReference(invoice.getReference());
        existingInvoice.setAttention(invoice.getAttention());
        existingInvoice.setIssueDate(invoice.getIssueDate());
        existingInvoice.setDueDate(invoice.getDueDate());
        existingInvoice.setStatus(invoice.getStatus());
        existingInvoice.setNotes(invoice.getNotes());
        existingInvoice.setTerms(invoice.getTerms());
        existingInvoice.setSubtotal(invoice.getSubtotal());
        existingInvoice.setTax(invoice.getTax());
        existingInvoice.setTaxRate(invoice.getTaxRate());
        existingInvoice.setDiscount(invoice.getDiscount());
        existingInvoice.setDiscountType(invoice.getDiscountType());
        existingInvoice.setTotalAmount(invoice.getTotalAmount());
        existingInvoice.setPaymentStatus(invoice.getPaymentStatus());
        existingInvoice.setPaymentMethod(invoice.getPaymentMethod());
        existingInvoice.setPaymentDate(invoice.getPaymentDate());
        
        // Clear existing items and add new ones
        existingInvoice.getInvoiceItems().clear();
        
        // Convert DTOs to InvoiceItem entities
        List<InvoiceItem> newInvoiceItems = convertDTOsToInvoiceItems(itemDTOs, existingInvoice);
        existingInvoice.setInvoiceItems(newInvoiceItems);
        
        return invoiceRepository.save(existingInvoice);
    }
    
    private List<InvoiceItem> convertDTOsToInvoiceItems(List<InvoiceItemDTO> itemDTOs, Invoices invoice) {
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        
        for (InvoiceItemDTO dto : itemDTOs) {
            // Fetch the product
            Products product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + dto.getProductId()));
            
            // Create InvoiceItem
            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.setInvoice(invoice);
            invoiceItem.setProduct(product);
            invoiceItem.setQuantity(dto.getQuantity());
            invoiceItem.setUnitPrice(dto.getUnitPrice());
            
            // Calculate total price
            if (dto.getTotalPrice() != null) {
                invoiceItem.setTotalPrice(dto.getTotalPrice());
            } else {
                invoiceItem.calculateTotalPrice();
            }
            
            invoiceItems.add(invoiceItem);
        }
        
        return invoiceItems;
    }
}