package com.example.DemoCheck.api;

import com.example.DemoCheck.entity.Employee;
import com.example.DemoCheck.entity.Office;
import com.example.DemoCheck.repository.EmployeeRepository;
import com.example.DemoCheck.repository.OfficeRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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

    @Autowired
    private OfficeRepository officeRepository;

    // --- HELPER METHOD ---
    // This creates and saves an employee in just one line of code
    private Employee createTestEmployee(Integer id, String firstName, String lastName, String email, String jobTitle, Integer reportsTo, Office office) {
        Employee emp = new Employee();
        emp.setEmployeeNumber(id);
        emp.setFirstName(firstName);
        emp.setLastName(lastName);
        emp.setEmail(email);
        emp.setExtension("x1234");
        emp.setJobTitle(jobTitle);
        emp.setReportsTo(reportsTo);
        emp.setOffice(office);
        return employeeRepository.save(emp);
    }

    private Office getDefaultOffice() {
        Office office = new Office();
        office.setOfficeCode("1");
        office.setCity("Nagpur");
        office.setPhone("1234567890");
        office.setAddressLine1("IT Park");
        office.setCountry("India");
        office.setPostalCode("440022");
        office.setTerritory("APAC");
        return officeRepository.save(office);
    }

    @Test
    void testGetAllEmployees_WithPagination() throws Exception {
        createTestEmployee(9999, "Atharva", "Bomle", "atharva@gmail.com", "Developer", null,getDefaultOffice());

        mockMvc.perform(get("/employees")
                        .param("projection", "employeeView")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.employees").exists());
    }

    @Test
    void getEmployeeById() throws Exception {
        createTestEmployee(9998, "John", "Doe", "john@gmail.com", "Manager", null,getDefaultOffice());

        mockMvc.perform(get("/employees/9998")
                        .param("projection", "employeeView"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@gmail.com"));
    }

    @Test
    void SearchByName_Pass() throws Exception {
        createTestEmployee(1001, "Atharva", "Bomle", "atharva.search@gmail.com", "Developer", null,getDefaultOffice());

        mockMvc.perform(get("/employees/search/byName")
                        .param("name", "Atharva Bomle")
                        .param("projection", "employeeView"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.employees").exists())
                .andExpect(jsonPath("$._embedded.employees[0].email").value("atharva.search@gmail.com"));
    }

    @Test
    void SearchByName_Fail() throws Exception {
        createTestEmployee(1001, "Atharva", "Bomle", "atharva.search@gmail.com", "Developer", null,getDefaultOffice());

        mockMvc.perform(get("/employees/search/byName")
                        .param("name", "Dhruv Toshniwal")
                        .param("projection", "employeeView"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.employees").isEmpty());
    }

    @Test
    void SearchByJobTitle_Pass() throws Exception {
        createTestEmployee(1003, "Alice", "Johnson", "alice.j@gmail.com", "Sales Manager (NA)", null,getDefaultOffice());

        mockMvc.perform(get("/employees/search/byJobTitle")
                        .param("jobTitle", "sales")
                        .param("projection", "employeeView"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.employees").exists())
                .andExpect(jsonPath("$._embedded.employees[0].jobTitle").value("Sales Manager (NA)"));
    }

    @Test
    void SearchByJobTitle_Fail() throws Exception {
        createTestEmployee(1003, "Alice", "Johnson", "alice.j@gmail.com", "Sales Manager (NA)", null,getDefaultOffice());

        mockMvc.perform(get("/employees/search/byJobTitle")
                        .param("jobTitle", "developer")
                        .param("projection", "employeeView"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.employees").isEmpty());
    }

    @Test
    void SearchByReportsTo_Pass() throws Exception {
        createTestEmployee(1, "Boss", "Manager", "boss@gmail.com", "CEO", null,getDefaultOffice());
        createTestEmployee(2, "Atharva", "Bomle", "atharva@gmail.com", "Sales Rep", 1,getDefaultOffice());

        mockMvc.perform(get("/employees/search/byReportsTo")
                        .param("reportsTo", "1")
                        .param("projection", "employeeView"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.employees").exists())
                .andExpect(jsonPath("$._embedded.employees[0].jobTitle").value("Sales Rep"));
    }

    @Test
    void SearchByReportsTo_Fail() throws Exception {
        createTestEmployee(1, "Boss", "Manager", "boss@gmail.com", "CEO", null,getDefaultOffice());
        createTestEmployee(2, "Atharva", "Bomle", "atharva@gmail.com", "Sales Rep", 1,getDefaultOffice());

        mockMvc.perform(get("/employees/search/byReportsTo")
                        .param("reportsTo", "999")
                        .param("projection", "employeeView"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.employees").isEmpty());
    }

    @Test
    void testCreateEmployee_Pass() throws Exception {
        // 1. Setup: Call your helper to ensure Office 1 is safely in the DB
        getDefaultOffice();

        String validJson = """
                {
                    "employeeNumber": 2000,
                    "firstName": "New",
                    "lastName": "Employee",
                    "email": "new.employee@gmail.com",
                    "jobTitle": "Backend Developer",
                    "extension": "x111",
                    "office": "/offices/1" 
                }
                """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.email").value("new.employee@gmail.com"));
    }

    @Test
    void testCreateEmployee_Fail_MissingMandatoryField() throws Exception {
        // 1. Setup: Call your helper
        getDefaultOffice();

        String invalidJson = """
                {
                    "employeeNumber": 2001,
                    "firstName": "Broken",
                    "lastName": "Employee",
                    "jobTitle": "Backend Developer",
                    "extension": "x111",
                    "office": "/offices/1" 
                }
                """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isConflict());
    }
}