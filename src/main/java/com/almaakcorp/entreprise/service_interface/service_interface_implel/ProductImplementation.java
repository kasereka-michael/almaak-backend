package com.almaakcorp.entreprise.service_interface.service_interface_implel;

import com.almaakcorp.entreprise.batchconfig.BatchConfiguration;
import com.almaakcorp.entreprise.models.Products;
import com.almaakcorp.entreprise.repositories.ProductRepository;
import com.almaakcorp.entreprise.service_interface.ProductInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class ProductImplementation implements ProductInterface {

    private final JobLauncher jobLauncher;
    private final BatchConfiguration batchConfiguration;
    private final ProductRepository productRepository;

    public ProductImplementation(JobLauncher jobLauncher, BatchConfiguration batchConfiguration, ProductRepository productRepository) {
        this.jobLauncher = jobLauncher;
        this.batchConfiguration = batchConfiguration;
        this.productRepository = productRepository;
    }

    /**
     * Imports products from an Excel file
     * @param filePath Path to the Excel file
     * @return JobExecution result
     */
    public JobExecution setUploadedPath(String filePath) {
        try {
            log.info("Starting import job for file: {}", filePath);
            if (!Files.exists(Paths.get(filePath))) {
                log.error("File does not exist: {}", filePath);
                throw new IllegalArgumentException("File does not exist: " + filePath);
            }
            Job job = batchConfiguration.createImportJob(filePath);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("filePath", filePath)
                    .addLong("startTime", System.currentTimeMillis())
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(job, jobParameters);
            log.info("Job execution status: {}", execution.getStatus());
            return execution;
        } catch (JobExecutionException e) {
            log.error("Batch job execution failed: {}", e.getMessage(), e);
            throw new RuntimeException("Batch job execution failed", e);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error", e);
        }
    }




    /**
     * Get products with pagination
     * @param pageNo Page number (0-based)
     * @param pageSize Number of items per page
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc or desc)
     * @return Page of products
     */
    @Override
    public Page<Products> getProductsPaginated(int pageNo, int pageSize, String sortBy, String sortDir, String search, String category, String status) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<Products> spec = Specification.where(null);

        // Search by productName, productPartNumber, manufacturer, or description
        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("productName")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("productPartNumber")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("productManufacturer")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("productDescription")), "%" + search.toLowerCase() + "%")
            ));
        }

        // Filter by category
        if (category != null && !category.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("productCategory"), category));
        }

        // Filter by status (based on quantity and minQuantity)
        if (status != null && !status.trim().isEmpty()) {
            if (status.equals("in-stock")) {
                spec = spec.and((root, query, cb) -> cb.gt(root.get("quantity"), 0));
            } else if (status.equals("low-stock")) {
                spec = spec.and((root, query, cb) -> cb.and(
                        cb.gt(root.get("quantity"), 0),
                        cb.le(root.get("quantity"), root.get("minQuantity"))
                ));
            } else if (status.equals("out-of-stock")) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("quantity"), 0));
            }
        }

        return productRepository.findAll(spec, pageable);
    }


    @Override
    public List<Products> getAllProducts() {
        return List.of();
    }

    @Override
    public Page<Products> getProductsPaginated(int pageNo, int pageSize, String sortBy, String sortDir) {
        return null;
    }

    @Override
    public Optional<Products> getProductById(Long id) {
        return Optional.ofNullable(productRepository.findById(id).orElse(null));
    }

    @Override
    public Products createProduct(Products product) {
        log.info("Creating product:::::::::::::::::::: {}", product.getProductName());
        Products checkProduct = productRepository.findByProductPartNumber(product.getProductPartNumber());
        if (checkProduct != null) {
            throw new IllegalArgumentException("Product with part number " + product.getProductPartNumber() + " already exists");
        }
        return productRepository.save(product);
    }

    @Override
    public Products updateProduct(Long id, Products product) {
        Optional<Products> existingProduct = productRepository.findByProductId(id);
        log.info("Existing product: and normal price {}", existingProduct.get().getProductNormalPrice());
        if (existingProduct.isPresent()) {
            existingProduct.get().setProductName(product.getProductName());
            existingProduct.get().setProductPartNumber(product.getProductPartNumber());
            existingProduct.get().setProductManufacturer(product.getProductManufacturer());
            existingProduct.get().setProductDescription(product.getProductDescription());
            existingProduct.get().setProductCategory(product.getProductCategory());
            existingProduct.get().setProductSellingPrice(product.getProductSellingPrice());
            existingProduct.get().setProductNormalPrice(product.getProductNormalPrice());
            existingProduct.get().setProductSKU(product.getProductSKU());
            existingProduct.get().setProductMinimumQuantity(product.getProductMinimumQuantity());
            existingProduct.get().setStorageLocation(product.getStorageLocation());
            existingProduct.get().setProductCurrentQuantity(product.getProductCurrentQuantity());
            existingProduct.get().setProductCostPrice(product.getProductCostPrice());
            existingProduct.get().setProductStatus(product.getProductStatus());
            existingProduct.get().setProductSupplierInfo(product.getProductSupplierInfo());
            existingProduct.get().setNotes(product.getNotes());
            existingProduct.get().setCreatedAt(product.getCreatedAt());
            existingProduct.get().setUpdatedAt(product.getUpdatedAt());
          return productRepository.save(existingProduct.get());
        }
        return null;
    }

    @Override
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<Products> searchProducts(String keyword, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.searchByName(keyword, pageable);
    }

    @Override
    public List<Products> getProductsByCategory(Long categoryId) {
        return List.of();
    }

    @Override
    public Products updateProductStock(Long productId, int quantity) {
        return null;
    }

    @Override
    public Products findByPartNumber(String partNumber) {
        return productRepository.findByProductPartNumber(partNumber);
    }


}
