package com.example.DemoCheck.handler;

import com.example.DemoCheck.entity.Employee;
import com.example.DemoCheck.repository.EmployeeRepository;
import com.example.DemoCheck.repository.OfficeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler(Employee.class)
public class EmployeeEventHandler {

    @Autowired
    private OfficeRepository officeRepository;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    @Autowired
    private EmployeeRepository employeeRepository;

    @HandleBeforeCreate
    public void beforeCreate(Employee employee) {
        checkDuplicate(employee);
        validate(employee);
    }

    @HandleBeforeSave
    public void beforeSave(Employee employee) {
        validate(employee);
    }

    private void checkDuplicate(Employee employee) {
        if (employee.getEmployeeNumber() != null &&
                employeeRepository.existsById(employee.getEmployeeNumber())) {
            throw new IllegalArgumentException(
                    "Employee already exists with id: " + employee.getEmployeeNumber()
            );
        }
    }

    private void validate(Employee employee) {
        if (employee.getEmployeeNumber() == null) {
            throw new IllegalArgumentException("employeeNumber cannot be null");
        }

        employee.setFirstName(normalize(employee.getFirstName(), "firstName"));
        employee.setLastName(normalize(employee.getLastName(), "lastName"));

        employee.setJobTitle(normalize(employee.getJobTitle(), "jobTitle"));

        String email = employee.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email cannot be blank");
        } else if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Must be a valid email format");
        }
        employee.setEmail(email.trim());

        // --- OFFICE VALIDATION ---
        if (employee.getOffice() != null) {
            String officeCode = employee.getOffice().getOfficeCode();

            if (officeCode == null || !officeRepository.existsById(officeCode)) {
                throw new IllegalArgumentException("Invalid office reference");
            }
        }

        // --- MANAGER VALIDATION ---
        if (employee.getManager() != null) {
            Integer managerId = employee.getManager().getEmployeeNumber();

            if (!employeeRepository.existsById(managerId)) {
                throw new IllegalArgumentException("Invalid manager reference");
            }

            if (managerId.equals(employee.getEmployeeNumber())) {
                throw new IllegalArgumentException("Employee cannot report to themselves");
            }
        }

    }

    private String normalize(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }
}
