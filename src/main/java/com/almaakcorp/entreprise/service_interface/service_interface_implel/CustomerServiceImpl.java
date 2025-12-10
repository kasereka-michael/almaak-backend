package com.almaakcorp.entreprise.service_interface.service_interface_implel;

import com.almaakcorp.entreprise.models.Customers;
import com.almaakcorp.entreprise.repositories.CustomerRepository;
import com.almaakcorp.entreprise.service_interface.CustomerInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerInterface {

    private final CustomerRepository customerRepository;


    @Override
    public List<Customers> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customers getCustomerById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    @Override
    public Customers createCustomer(Customers customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Customers updateCustomer(Long id, Customers customer) {
        Customers existing = customerRepository.findById(id).orElse(null);
        if (existing == null) return null;

        // Update mutable fields only if provided (non-null)
        if (customer.getName() != null) existing.setName(customer.getName());
        if (customer.getEmail() != null) existing.setEmail(customer.getEmail());
        if (customer.getPhone() != null) existing.setPhone(customer.getPhone());
        if (customer.getAddress() != null) existing.setAddress(customer.getAddress());
        if (customer.getStatus() != null) existing.setStatus(customer.getStatus());
        if (customer.getIndustry() != null) existing.setIndustry(customer.getIndustry());
        if (customer.getNotes() != null) existing.setNotes(customer.getNotes());
        if (customer.getWebsite() != null) existing.setWebsite(customer.getWebsite());


        return customerRepository.save(existing);
    }

    @Override
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    @Override
    public Page<Customers> getCustomersPaginated(int pageNo, int pageSize, String sortBy, String sortDir, String search, String status, String status1) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<Customers> spec = Specification.where(null);

        // Search by productName, productPartNumber, manufacturer, or description
        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("status")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("industry")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("notes")), "%" + search.toLowerCase() + "%")
            ));
        }

        // Filter by status
        if (status != null && !status.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }



        return customerRepository.findAll(spec, pageable);
    }
}
