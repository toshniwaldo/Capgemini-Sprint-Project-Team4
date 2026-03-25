package com.example.DemoCheck.api;

import com.example.DemoCheck.entity.Employee;
import com.example.DemoCheck.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EmployeeApiTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test

    void testGetAllEmployees() throws Exception {

        // Insert test data
        Employee emp = new Employee();
        emp.setEmployeeNumber(9999);
        emp.setFirstName("Atharva");
        emp.setLastName("Bomle");
        emp.setEmail("atharva@gmail.com");
        emp.setExtension("x1234");
        emp.setOfficeCode("1");
        emp.setJobTitle("Developer");

        employeeRepository.save(emp);

        // Call API
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.employees").exists());
    }

}
