package com.almaakcorp.entreprise.controllers;

import com.almaakcorp.entreprise.models.Customers;
import com.almaakcorp.entreprise.service_interface.service_interface_implel.CustomerServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/customer/v1")
@RequiredArgsConstructor
public class CustomerResources {

    private final CustomerServiceImpl customerService;


    @PostMapping("/save-customer")
    public ResponseEntity<Customers> createProduct(@RequestBody Customers customer) {
        if (customer.getName() == null || customer.getName().isEmpty()) {
            log.warn("Customer name is required");
            return ResponseEntity.badRequest().body(null);
        }
        Customers createdCustomer = customerService.createCustomer(customer);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }



    @GetMapping("/find-all")
    public ResponseEntity<CustomPageResponse> getAllCustomers(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
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
        List<String> validSortFields = Arrays.asList("id", "name", "notes", "status");
        if (!validSortFields.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sortBy field: " + sortBy);
        }

        // Validate status
        if (status != null && !List.of("lead","prospect","active","inactive").contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        try {
            Page<Customers> customersPage = customerService.getCustomersPaginated(pageNo, pageSize, sortBy, sortDir, search, status, status);
            CustomPageResponse customers = new CustomPageResponse(
                    customersPage.getContent(),
                    customersPage.getNumber(),
                    customersPage.getTotalElements(),
                    customersPage.getTotalPages(),
                    customersPage.isFirst(),
                    customersPage.isLast(),
                    customersPage.hasNext(),
                    customersPage.hasPrevious()
            );
            return new ResponseEntity<>(customers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching products: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public record CustomPageResponse(
            List<Customers> customer,
            int currentPage,
            long totalItems,
            int totalPages,
            boolean isFirst,
            boolean isLast,
            boolean hasNext,
            boolean hasPrevious
    ){}

    @GetMapping("/find-by-id/{id}")
    public ResponseEntity<Customers> getCustomerById(@PathVariable Long id) {
        Customers existing = customerService.getCustomerById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(existing);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Customers> updateCustomer(@PathVariable Long id, @RequestBody Customers customer) {
        try {
            Customers updated = customerService.updateCustomer(id, customer);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating customer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
