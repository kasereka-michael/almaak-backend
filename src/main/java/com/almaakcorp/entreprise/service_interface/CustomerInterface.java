package com.almaakcorp.entreprise.service_interface;

import com.almaakcorp.entreprise.models.Customers;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CustomerInterface {
    List<Customers> getAllCustomers();
    Customers getCustomerById(Long id);
    Customers createCustomer(Customers customer);
    Customers updateCustomer(Long id, Customers customer);
    void deleteCustomer(Long id);

    Page<Customers> getCustomersPaginated(int pageNo, int pageSize, String sortBy, String sortDir, String search, String status, String status1);
}
