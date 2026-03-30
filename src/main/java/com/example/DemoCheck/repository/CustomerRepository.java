package com.example.DemoCheck.repository;

import com.example.DemoCheck.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(path = "customer")
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    @Query("""
    SELECT c FROM Customer c
    WHERE 
        LOWER(c.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(c.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(c.country) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Customer> findCustomers(@Param("keyword") String keyword, Pageable pageable);

    @RestResource(path="by-employee", rel="by-employee")
    Page<Customer> findBySalesRepEmployee_EmployeeNumber(@Param("employeeNumber") Integer employeeNumber, Pageable pageable);
}




