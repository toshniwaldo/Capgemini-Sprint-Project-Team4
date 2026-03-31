package com.example.DemoCheck.service;

import com.example.DemoCheck.dto.CustomerPatchDTO;
import com.example.DemoCheck.entity.Customer;
import com.example.DemoCheck.entity.Employee;
import com.example.DemoCheck.repository.CustomerRepository;
import com.example.DemoCheck.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    public Customer patchCustomer(Integer id, CustomerPatchDTO dto) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // --- APPLY PATCH ---

        if (dto.getCustomerName() != null) {
            validateNotBlank(dto.getCustomerName(), "customerName");
            customer.setCustomerName(dto.getCustomerName().trim());
        }

        if (dto.getContactFirstName() != null) {
            validateNotBlank(dto.getContactFirstName(), "contactFirstName");
            customer.setContactFirstName(dto.getContactFirstName().trim());
        }

        if (dto.getContactLastName() != null) {
            validateNotBlank(dto.getContactLastName(), "contactLastName");
            customer.setContactLastName(dto.getContactLastName().trim());
        }

        if (dto.getPhone() != null) {
            if (!dto.getPhone().matches("^[0-9]{10}$")) {
                throw new IllegalArgumentException("phone must be exactly 10 digits");
            }
            customer.setPhone(dto.getPhone());
        }

        if (dto.getAddressLine1() != null) {
            validateNotBlank(dto.getAddressLine1(), "addressLine1");
            customer.setAddressLine1(dto.getAddressLine1().trim());
        }

        customer.setAddressLine2(dto.getAddressLine2());

        if (dto.getCity() != null) {
            validateNotBlank(dto.getCity(), "city");
            customer.setCity(dto.getCity().trim());
        }

        if (dto.getPostalCode() != null) {
            if (!dto.getPostalCode().matches("^[0-9]{6}$")) {
                throw new IllegalArgumentException("postalCode must be 6 digits");
            }
            customer.setPostalCode(dto.getPostalCode());
        }

        if (dto.getCountry() != null) {
            validateNotBlank(dto.getCountry(), "country");
            customer.setCountry(dto.getCountry().trim());
        }

        if (dto.getCreditLimit() != null) {
            if (dto.getCreditLimit().signum() < 0) {
                throw new IllegalArgumentException("creditLimit cannot be negative");
            }
            customer.setCreditLimit(dto.getCreditLimit());
        }

        if (dto.getSalesRepEmployeeNumber() != null) {
            Employee emp = employeeRepository.findById(dto.getSalesRepEmployeeNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or non-existing employee reference"));
            customer.setSalesRepEmployee(emp);
        }

        return customerRepository.save(customer);
    }

    private void validateNotBlank(String value, String field) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
    }
}
