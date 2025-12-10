package com.almaakcorp.entreprise.controllers;

import com.almaakcorp.entreprise.dto.QuotationRequestDTO;
import com.almaakcorp.entreprise.models.Quotations;
import com.almaakcorp.entreprise.service_interface.service_interface_implel.QuotationImplement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/quotation/v1")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class QuotationResources {

    private final QuotationImplement quotationImplementation;

    /**
     * Creates a new quotation
     *
     * @param quotationRequestDTO the quotation data to save
     * @return ResponseEntity containing the saved quotation or error details
     */
    @PostMapping("save-quotation")
    public ResponseEntity<?> saveQuotation(@RequestBody QuotationRequestDTO quotationRequestDTO) {
        log.info("REST request to save a new quotation: {}", quotationRequestDTO.getQuotationId());
        try {
            // Convert DTO to entity
            Quotations quotation = convertDTOToEntity(quotationRequestDTO);
            
            // Create quotation with items
            Quotations result = quotationImplementation.createQuotationWithItems(quotation, quotationRequestDTO.getItems());
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error saving quotation: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    Map.of("error", "Failed to save quotation", "message", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
    /**
     * Retrieves the last generated quotation ID from the system.
     * This endpoint is typically used to generate the next sequential quotation ID
     * for new quotation creation or to display the most recent quotation identifier.
     *
     * @return ResponseEntity containing:
     *         - On success: String representation of the last quotation ID with HTTP 200 OK
     *         - On error: Error message string with HTTP 500 INTERNAL_SERVER_ERROR
     *
     * @apiNote This endpoint does not require any parameters and returns the raw ID as a string
     * @since 1.0
     *
     * @example
     * GET /api/quotation/v1/get-last-id
     *
     * Success Response:
     * HTTP 200 OK
     * "QUO-2024-001"
     *
     * Error Response:
     * HTTP 500 Internal Server Error
     * "Error retrieving last ID"
     */
    @GetMapping("/get-last-id")
    public ResponseEntity<?> getLastId() {
        try {
            String lastId = quotationImplementation.getLastQuotationId();
            return new ResponseEntity<>(lastId, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting last ID: {}", e.getMessage());
            return new ResponseEntity<>("Error retrieving last ID", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    /**
     * Get paginated list of quotations
     *
     * @param pageNo   Page number (0-based)
     * @param pageSize Number of items per page
     * @param sortBy   Field to sort by (default: id)
     * @param sortDir  Sort direction (asc or desc, default: asc)
     * @return ResponseEntity with paginated quotations and metadata
     */

    @GetMapping("/find-all")
    public ResponseEntity<QuotationPageResponse> getAllQuotations(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "quotationId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Request for quotations page {} with size {}, sorted by {} {}, search: {}, category: {}, status: {}, startDate: {}, endDate: {}",
                pageNo, pageSize, sortBy, sortDir, search, category, status, startDate, endDate);

        // Validate sortDir
        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("sortDir must be 'asc' or 'desc'");
        }

        // Validate sortBy
        List<String> validSortFields = Arrays.asList("quotationId", "customerAddress", "reference", "attention");
        if (!validSortFields.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sortBy field: " + sortBy);
        }

        // Validate status
        if (status != null && !List.of("draft", "accepted","sent","rejected","expired").contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        try {
            Page<Quotations> quotationPage = quotationImplementation.getQuotationsPaginated(
                    pageNo, pageSize, sortBy, sortDir, search, category, status, startDate, endDate);
            QuotationPageResponse response = new QuotationPageResponse(
                    quotationPage.getContent(),
                    quotationPage.getNumber(),
                    quotationPage.getTotalElements(),
                    quotationPage.getTotalPages(),
                    quotationPage.isFirst(),
                    quotationPage.isLast(),
                    quotationPage.hasNext(),
                    quotationPage.hasPrevious()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching quotations: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public record QuotationPageResponse(
            List<Quotations> quotation,
            int currentPage,
            long totalItems,
            int totalPages,
            boolean isFirst,
            boolean isLast,
            boolean hasNext,
            boolean hasPrevious
    ){}

    /**
     * Get a quotation by its quotationId with associated products
     *
     * @param quotationId The unique quotation identifier
     * @return ResponseEntity containing the quotation with its products or error details
     */
    @GetMapping("/find-by-id/{quotationId}")
    public ResponseEntity<?> getQuotationByQuotationId(@PathVariable String quotationId) {
        log.info("REST request to find quotation by quotationId: {}", quotationId);
        try {
            Quotations quotation = quotationImplementation.getQuotationByQuotationId(quotationId);
            if (quotation == null) {
                return new ResponseEntity<>(
                        Map.of("error", "Quotation not found", "quotationId", quotationId),
                        HttpStatus.NOT_FOUND
                );
            }
            return new ResponseEntity<>(quotation, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error finding quotation by quotationId: {}", e.getMessage());
            return new ResponseEntity<>(
                    Map.of("error", "Failed to find quotation", "message", e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    /**
     * Get a quotation by its numeric ID with associated products
     *
     * @param id The unique numeric ID of the quotation
     * @return ResponseEntity containing the quotation with its products or error details
     */
    @GetMapping("/find/{id}")
    public ResponseEntity<?> getQuotationById(@PathVariable Long id) {
        log.info("REST request to find quotation by ID: {}", id);
        try {
            Quotations quotation = quotationImplementation.getQuotationById(id);
            if (quotation == null) {
                return new ResponseEntity<>(
                        Map.of("error", "Quotation not found", "id", id),
                        HttpStatus.NOT_FOUND
                );
            }
            return new ResponseEntity<>(quotation, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error finding quotation by ID: {}", e.getMessage());
            return new ResponseEntity<>(
                    Map.of("error", "Failed to find quotation", "message", e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Update an existing quotation
     *
     * @param id The unique quotation ID
     * @param quotationRequestDTO the updated quotation data
     * @return ResponseEntity containing the updated quotation or error details
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateQuotation(@PathVariable Long id, @RequestBody QuotationRequestDTO quotationRequestDTO) {
        log.info("REST request to update quotation with ID: {}", id);
        try {
            // Convert DTO to entity
            Quotations quotation = convertDTOToEntity(quotationRequestDTO);
            
            // Update quotation with items
            Quotations result = quotationImplementation.updateQuotationWithItems(id, quotation, quotationRequestDTO.getItems());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Error updating quotation: {}", e.getMessage());
            return new ResponseEntity<>(
                    Map.of("error", "Failed to update quotation", "message", e.getMessage()),
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            log.error("Error updating quotation: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    Map.of("error", "Failed to update quotation", "message", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * Delete a quotation by its ID (moves to trash)
     *
     * @param id The unique quotation ID
     * @return ResponseEntity with success or error message
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteQuotation(@PathVariable Long id) {
        log.info("REST request to delete quotation with ID: {}", id);
        try {
            // Check if quotation exists
            Quotations quotation = quotationImplementation.getQuotationById(id);
            if (quotation == null) {
                return new ResponseEntity<>(
                        Map.of("error", "Quotation not found", "id", id),
                        HttpStatus.NOT_FOUND
                );
            }


            
            // Move to trash instead of permanent deletion
            boolean moved = quotationImplementation.moveQuotationToTrash(id, "deletedBy", "deletedByName");
            if (moved) {
                return new ResponseEntity<>(
                        Map.of("message", "Quotation moved to trash successfully", "id", id),
                        HttpStatus.OK
                );
            } else {
                return new ResponseEntity<>(
                        Map.of("error", "Failed to move quotation to trash", "id", id),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
        } catch (Exception e) {
            log.error("Error deleting quotation: {}", e.getMessage());
            return new ResponseEntity<>(
                    Map.of("error", "Failed to delete quotation", "message", e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Restore a quotation from trash
     *
     * @param quotationData The quotation data from trash
     * @return ResponseEntity containing the restored quotation or error details
     */
    @PostMapping("/restore")
    public ResponseEntity<?> restoreQuotation(@RequestBody Map<String, Object> quotationData) {
        log.info("REST request to restore quotation from trash");
        try {
            Quotations restoredQuotation = quotationImplementation.restoreQuotationFromTrash(quotationData);
            return new ResponseEntity<>(restoredQuotation, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error restoring quotation: {}", e.getMessage());
            return new ResponseEntity<>(
                    Map.of("error", "Failed to restore quotation", "message", e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Generate PDF and optionally send via WhatsApp
     *
     * @param quotationId The quotation
     * @param payload Request containing WhatsApp details
     * @return ResponseEntity with success/error message
     */
    @PostMapping("/print-and-send/{quotationId}")
    public ResponseEntity<?> printAndSendQuotation(
            @PathVariable String quotationId,
            @RequestBody PrintAndSendPayload payload) {
        log.info("REST request to print and send quotation: {}", quotationId);
        try {
            // Validate payload and reconcile quotationId
            if (payload != null && payload.getQuotation() != null && payload.getQuotation().getQuotationId() != null) {
                String bodyQuotationId = payload.getQuotation().getQuotationId();
                if (!quotationId.equals(bodyQuotationId)) {
                    log.warn("Path quotationId ({}) differs from body quotationId ({}). Using path param.", quotationId, bodyQuotationId);
                }
            }

            Quotations quotation = quotationImplementation.getQuotationByQuotationId(quotationId);
            if (quotation == null) {
                return new ResponseEntity<>(
                        Map.of("error", "Quotation not found", "quotationId", quotationId),
                        HttpStatus.NOT_FOUND
                );
            }

            PrintAndSendRequest request = payload != null ? payload.getRequest() : null;
            if (request == null) {
                request = new PrintAndSendRequest();
            }

            quotationImplementation.printAndSendQuotation(quotation, request);
            return new ResponseEntity<>(Map.of("message", "PDF processed successfully"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error printing and sending quotation: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    Map.of("error", "Failed to print and send quotation", "message", e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Request DTO for print and send functionality
     */
    public static class PrintAndSendRequest {
        private boolean sendViaWhatsApp;
        private String whatsAppNumber;
        private String message;
        private ColumnOptions columnOptions;

        // Constructors
        public PrintAndSendRequest() {}

        public PrintAndSendRequest(boolean sendViaWhatsApp, String whatsAppNumber, String message) {
            this.sendViaWhatsApp = sendViaWhatsApp;
            this.whatsAppNumber = whatsAppNumber;
            this.message = message;
        }

        // Getters and setters
        public boolean isSendViaWhatsApp() {
            return sendViaWhatsApp;
        }

        public void setSendViaWhatsApp(boolean sendViaWhatsApp) {
            this.sendViaWhatsApp = sendViaWhatsApp;
        }

        public String getWhatsAppNumber() {
            return whatsAppNumber;
        }

        public void setWhatsAppNumber(String whatsAppNumber) {
            this.whatsAppNumber = whatsAppNumber;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public ColumnOptions getColumnOptions() {
            return columnOptions;
        }

        public void setColumnOptions(ColumnOptions columnOptions) {
            this.columnOptions = columnOptions;
        }
    }

    /**
     * Payload wrapper for print and send containing the full quotation form and request options
     */
    public static class PrintAndSendPayload {
        private QuotationRequestDTO quotation;
        private PrintAndSendRequest request;

        public PrintAndSendPayload() {}

        public QuotationRequestDTO getQuotation() {
            return quotation;
        }

        public void setQuotation(QuotationRequestDTO quotation) {
            this.quotation = quotation;
        }

        public PrintAndSendRequest getRequest() {
            return request;
        }

        public void setRequest(PrintAndSendRequest request) {
            this.request = request;
        }
    }

    /**
     * DTO for column options
     */
    public static class ColumnOptions {
        private boolean includeDescription = true;
        private boolean includePartNumber = true;
        private boolean includeManufacturer = true;
        private boolean includeManagerStamp = true;
        private boolean includeCompanyStamp = true;

        // Constructors
        public ColumnOptions() {}

        public ColumnOptions(boolean includeDescription, boolean includePartNumber, boolean includeManufacturer) {
            this.includeDescription = includeDescription;
            this.includePartNumber = includePartNumber;
            this.includeManufacturer = includeManufacturer;
        }
        
        public ColumnOptions(boolean includeDescription, boolean includePartNumber, boolean includeManufacturer, 
                           boolean includeManagerStamp, boolean includeCompanyStamp) {
            this.includeDescription = includeDescription;
            this.includePartNumber = includePartNumber;
            this.includeManufacturer = includeManufacturer;
            this.includeManagerStamp = includeManagerStamp;
            this.includeCompanyStamp = includeCompanyStamp;
        }

        // Getters and setters
        public boolean isIncludeDescription() {
            return includeDescription;
        }

        public void setIncludeDescription(boolean includeDescription) {
            this.includeDescription = includeDescription;
        }

        public boolean isIncludePartNumber() {
            return includePartNumber;
        }

        public void setIncludePartNumber(boolean includePartNumber) {
            this.includePartNumber = includePartNumber;
        }

        public boolean isIncludeManufacturer() {
            return includeManufacturer;
        }

        public void setIncludeManufacturer(boolean includeManufacturer) {
            this.includeManufacturer = includeManufacturer;
        }

        public boolean isIncludeManagerStamp() {
            return includeManagerStamp;
        }

        public void setIncludeManagerStamp(boolean includeManagerStamp) {
            this.includeManagerStamp = includeManagerStamp;
        }

        public boolean isIncludeCompanyStamp() {
            return includeCompanyStamp;
        }

        public void setIncludeCompanyStamp(boolean includeCompanyStamp) {
            this.includeCompanyStamp = includeCompanyStamp;
        }
    }
    
    /**
     * Helper method to convert QuotationRequestDTO to Quotations entity
     */
    private Quotations convertDTOToEntity(QuotationRequestDTO dto) {
        Quotations quotation = new Quotations();
        quotation.setId(dto.getId());
        quotation.setQuotationId(dto.getQuotationId());
        quotation.setCustomerId(dto.getCustomerId());
        quotation.setCustomerName(dto.getCustomerName());
        quotation.setCustomerEmail(dto.getCustomerEmail());
        quotation.setCustomerAddress(dto.getCustomerAddress());
        quotation.setReference(dto.getReference());
        quotation.setAttention(dto.getAttention());
        quotation.setValidUntil(dto.getValidUntil());
        quotation.setDownloadPath(dto.getDownloadPath());
        quotation.setStatus(dto.getStatus());
        quotation.setNotes(dto.getNotes());
        quotation.setTerms(dto.getTerms());
        quotation.setEta(dto.getEta());
        quotation.setSubtotal(dto.getSubtotal());
        quotation.setTax(dto.getTax());
        quotation.setTaxRate(dto.getTaxRate());
        quotation.setDiscount(dto.getDiscount());
        quotation.setDiscountType(dto.getDiscountType());
        quotation.setTotalAmount(dto.getTotalAmount());
        quotation.setExpectedIncome(dto.getExpectedIncome());
        return quotation;
    }
}
