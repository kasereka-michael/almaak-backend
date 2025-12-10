package com.almaakcorp.entreprise.repositories;

import com.almaakcorp.entreprise.models.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository  extends JpaRepository<Customers, Long>, JpaSpecificationExecutor<Customers> {

}
