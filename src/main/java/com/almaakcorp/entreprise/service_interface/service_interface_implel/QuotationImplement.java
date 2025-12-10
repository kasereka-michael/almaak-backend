package com.almaakcorp.entreprise.service_interface.service_interface_implel;

import com.almaakcorp.entreprise.dto.QuotationItemDTO;
import com.almaakcorp.entreprise.models.Products;
import com.almaakcorp.entreprise.models.QuotationItem;
import com.almaakcorp.entreprise.models.Quotations;
import com.almaakcorp.entreprise.repositories.ProductRepository;
import com.almaakcorp.entreprise.repositories.QuotationItemRepository;
import com.almaakcorp.entreprise.repositories.QuotationRepository;
import com.almaakcorp.entreprise.service_interface.QuotationInterface;
import com.almaakcorp.entreprise.controllers.QuotationResources;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationImplement implements QuotationInterface {

    private final QuotationRepository quotationRepository;
    private final QuotationItemRepository quotationItemRepository;
    private final ProductRepository productRepository;
    private final TrashImplementation trashService;
    private final PdfGenerationService pdfGenerationService;
    private final WhatsAppService whatsAppService;


    @Override
    public List<Quotations> getAllQuotations() {
        return List.of();
    }

    @Override
    public Quotations getQuotationById(Long id) {
        return quotationRepository.findByIdWithItems(id);
    }

    @Override
    public Quotations getQuotationByQuotationId(String quotationId) {
        return quotationRepository.findByQuotationIdWithItems(quotationId);
    }


    @Override
    @Transactional
    public Quotations createQuotation(Quotations quotation) {
        log.info("Creating new quotation with ID: {}", quotation.getQuotationId());
        
        // First save the quotation without items
        Quotations savedQuotation = quotationRepository.save(quotation);
        
        // Process and save quotation items if they exist
        if (quotation.getQuotationItems() != null && !quotation.getQuotationItems().isEmpty()) {
            processQuotationItems(savedQuotation, quotation.getQuotationItems());
        }
        
        log.info("Successfully created quotation with ID: {}", savedQuotation.getQuotationId());
        return quotationRepository.findByIdWithItems(savedQuotation.getId());
    }

    private void processQuotationItems(Quotations quotation, List<QuotationItem> quotationItems) {
        List<QuotationItem> processedItems = new ArrayList<>();
        
        for (QuotationItem item : quotationItems) {
            // Fetch the product from database to ensure it's managed
            Products product = productRepository.findById(item.getProduct().getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + item.getProduct().getProductId()));
            
            // Create new QuotationItem with proper relationships
            QuotationItem quotationItem = new QuotationItem();
            quotationItem.setQuotation(quotation);
            quotationItem.setProduct(product);
            quotationItem.setQuantity(item.getQuantity());
            quotationItem.setUnitPrice(item.getUnitPrice());
            quotationItem.calculateTotalPrice(); // Calculate total price
            
            processedItems.add(quotationItem);
        }
        
        // Clear existing items and add new ones
        quotation.getQuotationItems().clear();
        quotation.getQuotationItems().addAll(processedItems);
    }

    @Override
    @Transactional
    public Quotations updateQuotation(Long id, Quotations quotation) {
        log.info("Updating quotation with ID: {}", id);
        
        // Check if quotation exists
        Optional<Quotations> existingQuotationOpt = quotationRepository.findById(id);
        if (existingQuotationOpt.isEmpty()) {
            throw new RuntimeException("Quotation not found with ID: " + id);
        }
        
        Quotations existingQuotation = existingQuotationOpt.get();
        
        // Update quotation fields (only update non-null values)
        if (quotation.getQuotationId() != null) {
            existingQuotation.setQuotationId(quotation.getQuotationId());
        }
        if (quotation.getCustomerId() != null) {
            existingQuotation.setCustomerId(quotation.getCustomerId());
        }
        if (quotation.getCustomerName() != null) {
            existingQuotation.setCustomerName(quotation.getCustomerName());
        }
        if (quotation.getCustomerEmail() != null) {
            existingQuotation.setCustomerEmail(quotation.getCustomerEmail());
        }
        if (quotation.getCustomerAddress() != null) {
            existingQuotation.setCustomerAddress(quotation.getCustomerAddress());
        }
        if (quotation.getAttention() != null) {
            existingQuotation.setAttention(quotation.getAttention());
        }
        if (quotation.getReference() != null) {
            existingQuotation.setReference(quotation.getReference());
        }
        if (quotation.getStatus() != null) {
            existingQuotation.setStatus(quotation.getStatus());
        }
        if (quotation.getValidUntil() != null) {
            existingQuotation.setValidUntil(quotation.getValidUntil());
        }
        if (quotation.getTaxRate() != null) {
            existingQuotation.setTaxRate(quotation.getTaxRate());
        }
        if (quotation.getDiscount() != null) {
            existingQuotation.setDiscount(quotation.getDiscount());
        }
        if (quotation.getDiscountType() != null) {
            existingQuotation.setDiscountType(quotation.getDiscountType());
        }
        if (quotation.getSubtotal() != null) {
            existingQuotation.setSubtotal(quotation.getSubtotal());
        }
        if (quotation.getTax() != null) {
            existingQuotation.setTax(quotation.getTax());
        }
        if (quotation.getTotalAmount() != null) {
            existingQuotation.setTotalAmount(quotation.getTotalAmount());
        }
        if (quotation.getExpectedIncome() != null) {
            existingQuotation.setExpectedIncome(quotation.getExpectedIncome());
        }
        if (quotation.getNotes() != null) {
            existingQuotation.setNotes(quotation.getNotes());
        }
        if (quotation.getTerms() != null) {
            existingQuotation.setTerms(quotation.getTerms());
        }
        
        // Clear existing items and add new ones
        existingQuotation.getQuotationItems().clear();
        
        // Process items and set quotation relationship
        if (quotation.getQuotationItems() != null && !quotation.getQuotationItems().isEmpty()) {
            processQuotationItems(existingQuotation, quotation.getQuotationItems());
        }
        
        // Save updated quotation
        Quotations updatedQuotation = quotationRepository.save(existingQuotation);
        log.info("Successfully updated quotation with ID: {}", id);
        
        return updatedQuotation;
    }

    @Override
    @Transactional
    public void deleteQuotation(Long id) {
        quotationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public boolean moveQuotationToTrash(Long id, String deletedBy, String deletedByName) {
        log.info("Moving quotation with ID: {} to trash by user: {}", id, deletedBy);
        try {
            // Get the quotation with all its data
            Quotations quotation = quotationRepository.findByIdWithItems(id);
            if (quotation == null) {
                log.warn("Quotation not found with ID: {}", id);
                return false;
            }

            // Prepare entity data for trash
            Map<String, Object> entityData = new HashMap<>();
            entityData.put("id", quotation.getId());
            entityData.put("quotationId", quotation.getQuotationId());
            entityData.put("customerId", quotation.getCustomerId());
            entityData.put("customerName", quotation.getCustomerName());
            entityData.put("customerEmail", quotation.getCustomerEmail());
            entityData.put("customerAddress", quotation.getCustomerAddress());
            entityData.put("reference", quotation.getReference());
            entityData.put("attention", quotation.getAttention());
            entityData.put("validUntil", quotation.getValidUntil());
            entityData.put("downloadPath", quotation.getDownloadPath());
            entityData.put("status", quotation.getStatus());
            entityData.put("notes", quotation.getNotes());
            entityData.put("terms", quotation.getTerms());
            entityData.put("subtotal", quotation.getSubtotal());
            entityData.put("tax", quotation.getTax());
            entityData.put("taxRate", quotation.getTaxRate());
            entityData.put("discount", quotation.getDiscount());
            entityData.put("discountType", quotation.getDiscountType());
            entityData.put("totalAmount", quotation.getTotalAmount());
            entityData.put("expectedIncome", quotation.getExpectedIncome());
            entityData.put("createdAt", quotation.getCreatedAt());
            entityData.put("updatedAt", quotation.getUpdatedAt());

            // Include quotation items
            List<Map<String, Object>> itemsData = new ArrayList<>();
            if (quotation.getQuotationItems() != null) {
                for (QuotationItem item : quotation.getQuotationItems()) {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("id", item.getId());
                    itemData.put("quantity", item.getQuantity());
                    itemData.put("unitPrice", item.getUnitPrice());
                    itemData.put("totalPrice", item.getTotalPrice());
                    if (item.getProduct() != null) {
                        Map<String, Object> productData = new HashMap<>();
                        productData.put("productId", item.getProduct().getProductId());
                        productData.put("productName", item.getProduct().getProductName());
                        productData.put("description", item.getProduct().getProductDescription());
                        productData.put("price", item.getProduct().getProductSellingPrice());
                        productData.put("category", item.getProduct().getProductCategory());
                        itemData.put("product", productData);
                    }
                    itemsData.add(itemData);
                }
            }
            entityData.put("quotationItems", itemsData);

            // Move to trash
            String entityName = quotation.getQuotationId() + " - " + quotation.getCustomerName();
            trashService.moveToTrash("Quotation", id.toString(), entityName, entityData, deletedBy, deletedByName);

            // Delete from database
            quotationRepository.deleteById(id);

            log.info("Successfully moved quotation {} to trash", quotation.getQuotationId());
            return true;
        } catch (Exception e) {
            log.error("Error moving quotation to trash: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public Quotations restoreQuotationFromTrash(Map<String, Object> quotationData) {
        log.info("Restoring quotation from trash");
        try {
            // Create new quotation from trash data
            Quotations quotation = new Quotations();
            quotation.setQuotationId((String) quotationData.get("quotationId"));
            quotation.setCustomerId((String) quotationData.get("customerId"));
            quotation.setCustomerName((String) quotationData.get("customerName"));
            quotation.setCustomerEmail((String) quotationData.get("customerEmail"));
            quotation.setCustomerAddress((String) quotationData.get("customerAddress"));
            quotation.setReference((String) quotationData.get("reference"));
            quotation.setAttention((String) quotationData.get("attention"));
            
            // Handle date conversion
            if (quotationData.get("validUntil") != null) {
                Object validUntilObj = quotationData.get("validUntil");
                if (validUntilObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Integer> dateArray = (List<Integer>) validUntilObj;
                    if (dateArray.size() >= 3) {
                        quotation.setValidUntil(LocalDate.of(dateArray.get(0), dateArray.get(1), dateArray.get(2)));
                    }
                } else {
                    quotation.setValidUntil(LocalDate.parse(validUntilObj.toString()));
                }
            }
            
            // Handle createdAt date
            if (quotationData.get("createdAt") != null) {
                Object createdAtObj = quotationData.get("createdAt");
                if (createdAtObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Integer> dateArray = (List<Integer>) createdAtObj;
                    if (dateArray.size() >= 3) {
                        quotation.setCreatedAt(LocalDate.of(dateArray.get(0), dateArray.get(1), dateArray.get(2)));
                    }
                }
            }
            
            // Handle updatedAt date
            if (quotationData.get("updatedAt") != null) {
                Object updatedAtObj = quotationData.get("updatedAt");
                if (updatedAtObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Integer> dateArray = (List<Integer>) updatedAtObj;
                    if (dateArray.size() >= 3) {
                        quotation.setUpdatedAt(LocalDate.of(dateArray.get(0), dateArray.get(1), dateArray.get(2)));
                    }
                }
            }
            
            quotation.setDownloadPath((String) quotationData.get("downloadPath"));
            if (quotationData.get("status") != null) {
                quotation.setStatus(com.almaakcorp.entreprise.enums.Status.valueOf(quotationData.get("status").toString()));
            }
            quotation.setNotes((String) quotationData.get("notes"));
            quotation.setTerms((String) quotationData.get("terms"));
            
            // Handle numeric fields
            if (quotationData.get("subtotal") != null) {
                quotation.setSubtotal(Double.valueOf(quotationData.get("subtotal").toString()));
            }
            if (quotationData.get("tax") != null) {
                quotation.setTax(Double.valueOf(quotationData.get("tax").toString()));
            }
            if (quotationData.get("taxRate") != null) {
                quotation.setTaxRate(Double.valueOf(quotationData.get("taxRate").toString()));
            }
            if (quotationData.get("discount") != null) {
                quotation.setDiscount(Double.valueOf(quotationData.get("discount").toString()));
            }
            quotation.setDiscountType((String) quotationData.get("discountType"));
            if (quotationData.get("totalAmount") != null) {
                quotation.setTotalAmount(Double.valueOf(quotationData.get("totalAmount").toString()));
            }
            if (quotationData.get("expectedIncome") != null) {
                quotation.setExpectedIncome(Double.valueOf(quotationData.get("expectedIncome").toString()));
            }

            // Save the quotation first
            Quotations savedQuotation = quotationRepository.save(quotation);

            // Restore quotation items if they exist
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) quotationData.get("quotationItems");
            if (itemsData != null && !itemsData.isEmpty()) {
                List<QuotationItem> quotationItems = new ArrayList<>();
                
                for (Map<String, Object> itemData : itemsData) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> productData = (Map<String, Object>) itemData.get("product");
                    if (productData != null) {
                        // Find the product by ID
                        Long productId = Long.valueOf(productData.get("productId").toString());
                        Optional<Products> productOpt = productRepository.findById(productId);
                        
                        if (productOpt.isPresent()) {
                            QuotationItem item = new QuotationItem();
                            item.setQuotation(savedQuotation);
                            item.setProduct(productOpt.get());
                            item.setQuantity(Long.valueOf(Integer.valueOf(itemData.get("quantity").toString())));
                            item.setUnitPrice(BigDecimal.valueOf(Double.valueOf(itemData.get("unitPrice").toString())));
                            item.calculateTotalPrice();
                            quotationItems.add(item);
                        }
                    }
                }
                
                savedQuotation.getQuotationItems().addAll(quotationItems);
                savedQuotation = quotationRepository.save(savedQuotation);
            }

            log.info("Successfully restored quotation {} from trash", savedQuotation.getQuotationId());
            return savedQuotation;
        } catch (Exception e) {
            log.error("Error restoring quotation from trash: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to restore quotation from trash", e);
        }
    }

    @Override
    public String getLastQuotationId() {
        Quotations lastQuotation = quotationRepository.findFirstByOrderByQuotationIdDesc();
        return lastQuotation != null ? lastQuotation.getQuotationId() : null;
    }

    @Override
    public Page<Quotations> getQuotationsPaginated(
            int pageNo, int pageSize, String sortBy, String sortDir, String search, String category, String status,
            LocalDate startDate, LocalDate endDate) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<Quotations> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by name, status, reference, eta, or validUtil
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("quotationId")), searchPattern),
                        cb.like(cb.lower(root.get("status").as(String.class)), searchPattern),
                        cb.like(cb.lower(root.get("attention")), searchPattern),
                        cb.like(cb.lower(root.get("reference")), searchPattern),
                        cb.like(cb.lower(root.get("customerName")), searchPattern),
                        cb.like(cb.lower(root.get("customerEmail")), searchPattern)
                ));
            }

            // Filter by status
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), com.almaakcorp.entreprise.enums.Status.valueOf(status.toUpperCase())));
            }

            // Category filtering disabled (no 'category' field on Quotations entity)

            // Add date range filter if provided
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return quotationRepository.findAll(spec, pageable);
    }

    // Method to create quotation with DTOs (for frontend integration)
    @Transactional
    public Quotations createQuotationWithItems(Quotations quotation, List<QuotationItemDTO> itemDTOs) {
        log.info("Creating new quotation with items from DTOs");
        
        // First save the quotation without items
        Quotations savedQuotation = quotationRepository.save(quotation);
        
        // Convert DTOs to QuotationItems and process them
        if (itemDTOs != null && !itemDTOs.isEmpty()) {
            List<QuotationItem> quotationItems = convertDTOsToQuotationItems(itemDTOs);
            processQuotationItems(savedQuotation, quotationItems);
        }
        
        return quotationRepository.findByIdWithItems(savedQuotation.getId());
    }
    
    // Method to update quotation with DTOs
    @Transactional
    public Quotations updateQuotationWithItems(Long id, Quotations quotation, List<QuotationItemDTO> itemDTOs) {
        log.info("Updating quotation with ID: {} with items from DTOs", id);
        
        // Update the quotation first
        Quotations updatedQuotation = updateQuotation(id, quotation);
        
        // Clear existing items and add new ones from DTOs
        updatedQuotation.getQuotationItems().clear();
        
        if (itemDTOs != null && !itemDTOs.isEmpty()) {
            List<QuotationItem> quotationItems = convertDTOsToQuotationItems(itemDTOs);
            processQuotationItems(updatedQuotation, quotationItems);
        }
        
        return quotationRepository.save(updatedQuotation);
    }
    
    private List<QuotationItem> convertDTOsToQuotationItems(List<QuotationItemDTO> itemDTOs) {
        List<QuotationItem> quotationItems = new ArrayList<>();
        
        for (QuotationItemDTO dto : itemDTOs) {
            // Fetch the product from database
            Products product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + dto.getProductId()));
            
            QuotationItem item = new QuotationItem();
            item.setProduct(product);
            item.setQuantity(dto.getQuantity());
            item.setUnitPrice(dto.getUnitPrice());
            item.calculateTotalPrice();
            
            quotationItems.add(item);
        }
        
        return quotationItems;
    }

    /**
     * Generate PDF and optionally send via WhatsApp
     */
    public Quotations printAndSendQuotation(Quotations quotations, QuotationResources.PrintAndSendRequest request) {
        log.info("Processing print and send request for quotation: {}", quotations);
        
        try {


            // Generate PDF with column options
            byte[] pdfBytes = pdfGenerationService.generateQuotationPdf(quotations, request.getColumnOptions());
            log.info("PDF generated successfully for quotation: {}", quotations);

            String result = "PDF generated successfully";

            // Send via WhatsApp if requested
            if (request.isSendViaWhatsApp()) {
                if (request.getWhatsAppNumber() == null || request.getWhatsAppNumber().trim().isEmpty()) {
                    throw new RuntimeException("WhatsApp number is required when sending via WhatsApp");
                }

                if (!whatsAppService.isConfigured()) {
                    throw new RuntimeException("WhatsApp service is not properly configured. Please check Green API credentials.");
                }

                String fileName = " " + quotations.getQuotationId() + ".pdf";
                String caption = request.getMessage() != null && !request.getMessage().trim().isEmpty() 
                    ? request.getMessage()
                    : "A quotation has been printed from Almaakcorp system kindly review and provide feedback :quotation: " + quotations.getQuotationId() +"\nEmail Subject: " + quotations.getReference();

                // Send the PDF via WhatsApp
                String whatsAppResult = whatsAppService.sendPdfDocument(
                    request.getWhatsAppNumber(), 
                    pdfBytes, 
                    fileName, 
                    caption
                ).block(); // Block to wait for the result

                result += " and sent via WhatsApp: " + whatsAppResult;
                log.info("WhatsApp sent successfully for quotation: {}", quotations.getQuotationId());
            }

            return quotations;

        } catch (Exception e) {
            log.error("Error in printAndSendQuotation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process print and send request: " + e.getMessage(), e);
        }
    }

}
