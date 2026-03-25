package com.example.DemoCheck.repository;

import com.example.DemoCheck.entity.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("Test: Get All Employees")
    public void getAllEmployees(){
        int employeesOld = employeeRepository.findAll().size();
        // Step 1: Create dummy data
        Employee emp = new Employee();
        emp.setEmployeeNumber(9999);
        emp.setFirstName("Atharva");
        emp.setLastName("Bomle");
        emp.setEmail("atharva@gmail.com");
        emp.setExtension("x1234");
        emp.setOfficeCode("1");
        emp.setJobTitle("Developer");

        employeeRepository.save(emp);
        // Step 2: Fetch data
        int employeesNew = employeeRepository.findAll().size();

        // Step 3: Assertions
        assertEquals(employeesOld+1, employeesNew);
    }
}
