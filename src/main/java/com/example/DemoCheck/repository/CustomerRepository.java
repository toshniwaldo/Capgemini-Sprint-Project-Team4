package com.example.DemoCheck.repository;

import com.example.DemoCheck.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
