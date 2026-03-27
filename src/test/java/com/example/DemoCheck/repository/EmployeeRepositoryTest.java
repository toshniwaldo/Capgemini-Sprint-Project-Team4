package com.example.DemoCheck.repository;

import com.example.DemoCheck.entity.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    // Test Case for Find all
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
//        emp.setOfficeCode("1");
        emp.setJobTitle("Developer");

        employeeRepository.save(emp);
        // Step 2: Fetch data
        int employeesNew = employeeRepository.findAll().size();

        // Step 3: Assertions
        assertEquals(employeesOld+1, employeesNew);
    }


    // Test Case for Pagination and 1st Page

    @Test
    @DisplayName("Test: Pagination - findAll with pageable")
    public void testPagination(){

        for(int i=0;i<50;i++){
            Employee emp = new Employee();
            emp.setEmployeeNumber(i);
            emp.setFirstName("Test" + i);
            emp.setLastName("User");
            emp.setEmail("test" + i + "@gmail.com");
            emp.setExtension("x1234");
//            emp.setOfficeCode("1");
            emp.setJobTitle("Developer");

            employeeRepository.save(emp);
        }

        Pageable pageable = PageRequest.of(0,5);

        Page<Employee> page = employeeRepository.findAll(pageable);

        assertEquals(5,page.getContent().size()); // only 5 records
        assertEquals(0,page.getNumber());         // page number
        assertTrue(page.getTotalPages()>=10);     // total records
    }


    // Testing 2nd Page

    @Test
    @DisplayName("Test: Pagination - second page")
    public void testSecondPage() {

        Pageable pageable = PageRequest.of(1, 5); // page 1

        Page<Employee> page = employeeRepository.findAll(pageable);

        assertEquals(5, page.getSize());
        assertEquals(1, page.getNumber());
    }



}
