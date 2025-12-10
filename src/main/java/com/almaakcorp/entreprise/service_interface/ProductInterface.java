package com.almaakcorp.entreprise.service_interface;

import java.util.List;
import java.util.Optional;

import com.almaakcorp.entreprise.models.Products;
import org.springframework.batch.core.JobExecution;
import org.springframework.data.domain.Page;

/**
 * Interface defining the contract for product-related operations
 * in the AlmaakCorp enterprise application.
 */
public interface ProductInterface {

    /**
     * Retrieves all products from the system.
     *
     * @return List of all products
     */
    List<Products> getAllProducts();

    /**
     * Get products with pagination
     * @param pageNo Page number (0-based)
     * @param pageSize Number of items per page
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc or desc)
     * @return Page of products
     */
    Page<Products> getProductsPaginated(int pageNo, int pageSize, String sortBy, String sortDir);


    JobExecution setUploadedPath(String path);
    Page<Products> getProductsPaginated(int pageNo, int pageSize, String sortBy, String sortDir, String search, String category, String status);
    Products findByPartNumber(String partNumber);
    /**
     * Retrieves a product by its unique identifier.
     *
     * @param id The unique identifier of the product
     * @return An Optional containing the product if found, empty otherwise
     */
    Optional<Products> getProductById(Long id);

    /**
     * Creates a new product in the system.
     *
     * @param product The product to be created
     * @return The created product with assigned ID
     */
    Products createProduct(Products product);

    /**
     * Updates an existing product.
     *
     * @param id The ID of the product to update
     * @param product The updated product data
     * @return The updated product
     * @throws RuntimeException if the product doesn't exist
     */
    Products updateProduct(Long id, Products product);

    /**
     * Deletes a product from the system.
     *
     * @param id The ID of the product to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteProduct(Long id);

    /**
     * Searches for products by name or description.
     *
     * @param keyword The search keyword
     * @param limit The limit on the number of results
     * @return List of products matching the search criteria
     */
    List<Products> searchProducts(String keyword, int limit);

    /**
     * Retrieves products by category.
     *
     * @param categoryId The category ID
     * @return List of products in the specified category
     */
    List<Products> getProductsByCategory(Long categoryId);

    /**
     * Updates the stock quantity of a product.
     *
     * @param productId The product ID
     * @param quantity The new quantity
     * @return The updated product
     */
    Products updateProductStock(Long productId, int quantity);
}
