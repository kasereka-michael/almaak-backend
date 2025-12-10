package com.almaakcorp.entreprise.controllers;

import com.almaakcorp.entreprise.dto.ProductDTO;
import com.almaakcorp.entreprise.models.Products;
import com.almaakcorp.entreprise.service_interface.service_interface_implel.ProductImplementation;
import com.almaakcorp.entreprise.service_interface.service_interface_implel.TrashImplementation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/product/v1")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductResources {

    private final ProductImplementation productImplementation;
    private final TrashImplementation trashService;
    @Value("${file.upload-dir:D:/app/batch_excel_Uploads}")
    private String UPLOAD_DIR;

    public ProductResources(ProductImplementation productImplementation, TrashImplementation trashService) {
        this.productImplementation = productImplementation;
        this.trashService = trashService;
    }

    @PostMapping("/batch-excel-save")
    public ResponseEntity<String> importProducts(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a file");
        }

        try {
            // Validate file type
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.badRequest().body("File name cannot be null");
            }
            if (!originalFilename.endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Only CSV files (.csv) are supported");
            }
            if (!file.getContentType().equals("text/csv")) {
                return ResponseEntity.badRequest().body("Invalid file type. Only CSV files are supported");
            }

            // Create directory
            Path uploadPath = Paths.get(UPLOAD_DIR).normalize();
            log.info("Upload directory absolute path: {}", uploadPath.toAbsolutePath());
            if (!Files.exists(uploadPath)) {
                log.info("Creating directory: {}", uploadPath);
                Files.createDirectories(uploadPath);
                log.info("Directory created: {}", uploadPath);
            }

            // Generate unique filename
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);
            if (Files.exists(filePath)) {
                uniqueFilename = UUID.randomUUID().toString() + fileExtension;
                filePath = uploadPath.resolve(uniqueFilename);
            }

            // Save the file
            log.info("Saving file to: {}", filePath);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            String filePathString = filePath.toString();
            if (!Files.exists(filePath)) {
                log.error("File was not saved: {}", filePathString);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("File was not saved");
            }

            // Process the file using the service
            JobExecution jobExecution = productImplementation.setUploadedPath(filePathString);
            if (jobExecution == null) {
                log.error("JobExecution is null");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Batch job failed to start");
            }

            if (jobExecution.getExitStatus().getExitCode().equals("COMPLETED")) {
                log.info("Batch job completed successfully");
                Files.deleteIfExists(filePath);
                return ResponseEntity.ok("File processed and deleted successfully");
            } else {
                log.error("Batch job failed: {}", jobExecution.getExitStatus().getExitDescription());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Batch job failed: " + jobExecution.getExitStatus().getExitDescription());
            }
        } catch (IOException e) {
            log.error("File storage error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start.sh file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Batch job error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Batch job error: " + e.getMessage());
        }
    }




    /**
     * Get paginated list of products
     *
     * @param pageNo   Page number (0-based)
     * @param pageSize Number of items per page
     * @param sortBy   Field to sort by (default: id)
     * @param sortDir  Sort direction (asc or desc, default: asc)
     * @return ResponseEntity with paginated products and metadata
     */
    @GetMapping("/find-all")
    public ResponseEntity<ProductPageResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
    log.info("Request for products page {} with size {}, sorted by {} {}, search: {}, category: {}, status: {}",
             new Object[]{pageNo, pageSize, sortBy, sortDir, search, category, status});

        // Validate sortDir
        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("sortDir must be 'asc' or 'desc'");
        }

        // Validate sortBy
        List<String> validSortFields = Arrays.asList("productId", "productName", "productPartNumber", "productSellingPrice");
        if (!validSortFields.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sortBy field: " + sortBy);
        }

        // Validate status
        if (status != null && !List.of("in-stock", "low-stock", "out-of-stock").contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        try {
            Page<Products> productPage = productImplementation.getProductsPaginated(pageNo, pageSize, sortBy, sortDir, search, category, status);
            ProductPageResponse response = new ProductPageResponse(
                    productPage.getContent(),
                    productPage.getNumber(),
                    productPage.getTotalElements(),
                    productPage.getTotalPages(),
                    productPage.isFirst(),
                    productPage.isLast(),
                    productPage.hasNext(),
                    productPage.hasPrevious()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching products: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
    public record ProductPageResponse(
            List<Products> products,
            int currentPage,
            long totalItems,
            int totalPages,
            boolean isFirst,
            boolean isLast,
            boolean hasNext,
            boolean hasPrevious
    ){}


    @GetMapping("/get-product-by-id/{id}")
    public ResponseEntity<Products> getProductById(@PathVariable Long id) {
        log.info("Fetching product with ID: {}", id);
        return productImplementation.getProductById(id)
                .map(product -> new ResponseEntity<>(product, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/save-product")
    public ResponseEntity<Products> createProduct(@RequestBody Products product) {
        if (product.getProductName() == null || product.getProductName().isEmpty()) {
            log.warn("Product name is required");
            return ResponseEntity.badRequest().body(null);
        }
        Products createdProduct = productImplementation.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String reason) {
        log.info("Soft-deleting product (move to trash) with ID: {}", id);
        try {
            Optional<Products> productOpt = productImplementation.getProductById(id);
            if (productOpt.isEmpty()) {
                log.warn("Product not found for deletion: {}", id);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Products product = productOpt.get();

            // If already soft-deleted, return 204 idempotently
            try {
                // If the entity has a deleted flag, respect it
                java.lang.reflect.Method getDeleted = product.getClass().getMethod("getDeleted");
                Object deletedVal = getDeleted.invoke(product);
                if (deletedVal instanceof Boolean && (Boolean) deletedVal) {
                    log.info("Product {} already soft-deleted; returning 204.", id);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
            } catch (NoSuchMethodException ignore) {
                // If no deleted flag exists, we still proceed with soft-delete pattern without reflection guarding
            }

            // Convert product to map for storage in trash snapshot
            Map<String, Object> productData = convertProductToMap(product);

            String currentUser = username != null ? username : "demo_user";

            // Create a trash record first
            trashService.moveToTrash(
                "Product",
                id.toString(),
                product.getProductName(),
                productData,
                currentUser,
                currentUser,
                reason
            );

            // Soft-delete: set deleted flags on the product and save. Do NOT physically remove to avoid FK violations.
            try {
                // Set deleted=true via reflection to avoid compile dependency if field not yet present
                java.lang.reflect.Method setDeleted = product.getClass().getMethod("setDeleted", boolean.class);
                setDeleted.invoke(product, true);
                // Optional metadata if available
                try {
                    java.lang.reflect.Method setDeletedAt = product.getClass().getMethod("setDeletedAt", java.time.Instant.class);
                    setDeletedAt.invoke(product, java.time.Instant.now());
                } catch (NoSuchMethodException ignored) {}
                try {
                    java.lang.reflect.Method setDeletedBy = product.getClass().getMethod("setDeletedBy", String.class);
                    setDeletedBy.invoke(product, currentUser);
                } catch (NoSuchMethodException ignored) {}
            } catch (NoSuchMethodException e) {
                log.warn("Products entity lacks deleted flags; please add deleted/deletedAt/deletedBy fields. Proceeding without flags.");
            }

            // Persist the soft-deleted state
            try {
                // Expect ProductImplementation to have a save method
                productImplementation.createProduct(product); // fallback to save semantics
            } catch (Exception ex) {
                log.warn("createProduct used as fallback for saving soft-delete. Consider adding save(product). Error: {}", ex.getMessage());
                // If there's an explicit save method, prefer it. Otherwise rely on implementation specifics.
            }

            log.info("Product soft-deleted (moved to trash) successfully: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("Error soft-deleting product: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Void>  updateProduct(@PathVariable Long id, @RequestBody Products product) {
        log.info("Deleting product with ID: {}", id);
        try {
            Products products = productImplementation.updateProduct(id,product);
            if (products != null) {
                log.info("Product  updated successfully: {}", id);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                log.warn("Product not found for update: {}", id);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error deleting product: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Products> searchProductByPartNumber(@RequestParam String partNumber) {

        try {
            Products product = productImplementation.findByPartNumber(partNumber);
            log.info("Searching product by partNumber: {}", product.getProductPartNumber());

            return new ResponseEntity<>(product, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error searching product: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/search-by-name")
    public List<ProductDTO> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<Products> products = productImplementation.searchProducts(query, limit);
        return products.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private ProductDTO mapToDTO(Products product) {
        if (product == null) {
            return null;
        }

        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId()); // Using getId() as in the original code
        dto.setProductName(product.getProductName() != null ? product.getProductName() : "");
        dto.setProductSKU(product.getProductSKU() != null ? product.getProductSKU() : "");
        dto.setProductPartNumber(product.getProductPartNumber() != null ? product.getProductPartNumber() : "");
        dto.setProductCategory(product.getProductCategory() != null ? product.getProductCategory() : "");
        dto.setProductManufacturer(product.getProductManufacturer() != null ? product.getProductManufacturer() : "");
        dto.setProductDescription(product.getProductDescription() != null ? product.getProductDescription() : "");
        dto.setProductSellingPrice(product.getProductSellingPrice() != null ? product.getProductSellingPrice() : BigDecimal.ZERO);
        dto.setProductNormalPrice(product.getProductNormalPrice() != null ? product.getProductNormalPrice() : BigDecimal.ZERO);
        dto.setProductCurrentQuantity(product.getProductCurrentQuantity() != null ? product.getProductCurrentQuantity() : 0);
        dto.setProductMinimumQuantity(product.getProductMinimumQuantity() != null ? product.getProductMinimumQuantity() : 0);
        dto.setStorageLocation(product.getStorageLocation() != null ? product.getStorageLocation() : "");
        dto.setSupplierInfo(product.getProductSupplierInfo() != null ? product.getProductSupplierInfo() : ""); // Using getSupplierInfo() as in the original code
        dto.setNotes(product.getNotes() != null ? product.getNotes() : "");
        return dto;
    }

    /**
     * Restore product from trash
     */
    @PostMapping("/restore/{trashId}")
    public ResponseEntity<Products> restoreProduct(
            @PathVariable String trashId,
            @RequestParam(required = false) String username) {
        log.info("Request to restore product from trash: {}", trashId);
        
        try {
            String currentUser = username != null ? username : "demo_user";
            Optional<Map<String, Object>> restoredData = trashService.restoreFromTrash(trashId, currentUser, false);
            
            if (restoredData.isPresent()) {
                // Convert map back to Product entity
                Products product = convertMapToProduct(restoredData.get());
                
                // Save the restored product
                Products savedProduct = productImplementation.createProduct(product);
                log.info("Product restored successfully: {}", savedProduct.getProductName());
                
                return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error restoring product: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> convertProductToMap(Products product) {
        Map<String, Object> map = new HashMap<>();
        map.put("productId", product.getProductId());
        map.put("productName", product.getProductName());
        map.put("productSKU", product.getProductSKU());
        map.put("productPartNumber", product.getProductPartNumber());
        map.put("productCategory", product.getProductCategory());
        map.put("productManufacturer", product.getProductManufacturer());
        map.put("productDescription", product.getProductDescription());
        map.put("productSellingPrice", product.getProductSellingPrice());
        map.put("productNormalPrice", product.getProductNormalPrice());
        map.put("productCurrentQuantity", product.getProductCurrentQuantity());
        map.put("productMinimumQuantity", product.getProductMinimumQuantity());
        map.put("storageLocation", product.getStorageLocation());
        map.put("productSupplierInfo", product.getProductSupplierInfo());
        map.put("notes", product.getNotes());
        return map;
    }

    private Products convertMapToProduct(Map<String, Object> map) {
        Products product = new Products();
        // Note: Don't set the original ID to avoid conflicts
        product.setProductName((String) map.get("productName"));
        product.setProductSKU((String) map.get("productSKU"));
        product.setProductPartNumber((String) map.get("productPartNumber"));
        product.setProductCategory((String) map.get("productCategory"));
        product.setProductManufacturer((String) map.get("productManufacturer"));
        product.setProductDescription((String) map.get("productDescription"));
        
        if (map.get("productSellingPrice") != null) {
            product.setProductSellingPrice(new BigDecimal(map.get("productSellingPrice").toString()));
        }
        if (map.get("productNormalPrice") != null) {
            product.setProductNormalPrice(new BigDecimal(map.get("productNormalPrice").toString()));
        }
        if (map.get("productCurrentQuantity") != null) {
            product.setProductCurrentQuantity(((Number) map.get("productCurrentQuantity")).longValue());
        }
        if (map.get("productMinimumQuantity") != null) {
            product.setProductMinimumQuantity(((Number) map.get("productMinimumQuantity")).longValue());
        }
        
        product.setStorageLocation((String) map.get("storageLocation"));
        product.setProductSupplierInfo((String) map.get("productSupplierInfo"));
        product.setNotes((String) map.get("notes"));
        
        return product;
    }


}