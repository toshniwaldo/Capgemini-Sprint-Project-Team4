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

import static org.junit.jupiter.api.Assertions.assertTrue;
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
        emp.setOffice(office);

        // ✅ FIX HERE
        if (reportsTo != null) {
            Employee manager = employeeRepository.findById(reportsTo).orElse(null);
            emp.setManager(manager);
        }

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

//    @Test
//    void testOfficeCreated() {
//        Office office = getDefaultOffice();
//
//        assertTrue(officeRepository.findById("1").isPresent());
//    }

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
                .andExpect(header().exists("Location"));
    }


    @Test
    void testCreateEmployee_Fail_MissingFirstName() throws Exception {

        getDefaultOffice();

        String invalidJson = """
            {
                "employeeNumber": 2001,
                "firstName": "Broken",
                "lastName": "",
                "jobTitle": "Backend Developer",
                "extension": "x111",
                "office": "/offices/1"
            }
            """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("lastName cannot be blank"));; // ✅ FIX
    }

    @Test
    void testCreateEmployee_Fail_NullEmployeeNumber() throws Exception {
        getDefaultOffice();
        String invalidJson = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "email": "john.doe@test.com",
                "jobTitle": "Backend Developer",
                "office": "/offices/1"
            }
            """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("employeeNumber cannot be null"));
    }

    @Test
    void testCreateEmployee_Fail_DuplicateEmployeeNumber() throws Exception {
        Office savedOffice = getDefaultOffice();
        String officeId = savedOffice.getOfficeCode();

        // First insert to ensure the ID exists
        String validJson = """
            {
                "employeeNumber": 2005,
                "firstName": "Original",
                "lastName": "User",
                "email": "original@test.com",
                "extension": "x123",
                "jobTitle": "Developer",
                "office": "/offices/%s"
            }
            """.formatted(officeId);;
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated());

        // Attempt duplicate insert
        String duplicateJson = """
            {
                "employeeNumber": 2005,
                "firstName": "Duplicate",
                "lastName": "User",
                "email": "duplicate@test.com",
                "extension": "x123",
                "jobTitle": "Developer",
                "office": "/offices/%s"
            }
            """.formatted(officeId);;
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Employee already exists with id: 2005"));
    }

    @Test
    void testCreateEmployee_Fail_BlankFirstName() throws Exception {
        getDefaultOffice();
        String invalidJson = """
            {
                "employeeNumber": 2002,
                "firstName": "   ",
                "lastName": "Doe",
                "email": "john.doe@test.com",
                "jobTitle": "Backend Developer",
                "office": "/offices/1"
            }
            """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("firstName cannot be blank"));
    }

    @Test
    void testCreateEmployee_Fail_BlankEmail() throws Exception {
        getDefaultOffice();
        String invalidJson = """
            {
                "employeeNumber": 2003,
                "firstName": "John",
                "lastName": "Doe",
                "email": "",
                "jobTitle": "Backend Developer",
                "office": "/offices/1"
            }
            """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email cannot be blank"));
    }

    @Test
    void testCreateEmployee_Fail_InvalidEmailFormat() throws Exception {
        getDefaultOffice();
        String invalidJson = """
            {
                "employeeNumber": 2004,
                "firstName": "John",
                "lastName": "Doe",
                "email": "invalid-email-format",
                "jobTitle": "Backend Developer",
                "office": "/offices/1"
            }
            """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Must be a valid email format"));
    }


    @Test
    void UpdateEmployee_JobTitle_Pass() throws Exception {
        Office office = getDefaultOffice();
        createTestEmployee(3007, "John", "Doe", "john@test.com", "Dev", null, office);

        String updateJson = """
        {
            "jobTitle": "Senior Developer"
        }
        """;

        mockMvc.perform(patch("/employees/3007")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void UpdateEmployee_JobTitle_Fail_Blank() throws Exception {
        Office office = getDefaultOffice();
        createTestEmployee(3008, "John", "Doe", "john@test.com", "Dev", null, office);

        String updateJson = """
        {
            "jobTitle": ""
        }
        """;

        mockMvc.perform(patch("/employees/3008")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("jobTitle cannot be blank"));
    }

    @Test
    void UpdateEmployee_Office_Pass() throws Exception {
        Office office1 = getDefaultOffice();
        Office office2 = new Office();
        office2.setOfficeCode("2");
        office2.setCity("Mumbai");
        officeRepository.save(office2);

        createTestEmployee(3009, "John", "Doe", "john@test.com", "Dev", null, office1);

        String updateJson = """
        {
            "office": "/offices/2"
        }
        """;

        mockMvc.perform(patch("/employees/3009")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void UpdateEmployee_Office_Fail_NotFound() throws Exception {
        Office office = getDefaultOffice();
        createTestEmployee(3010, "John", "Doe", "john@test.com", "Dev", null, office);

        String updateJson = """
        {
            "office": "/offices/999"
        }
        """;

        // Spring Data REST usually returns 400 or 404 for broken links
        // depending on the version. If you expect 400, ensure your
        // ExceptionHandler handles ResourceNotFoundException during binding.
        mockMvc.perform(patch("/employees/3010")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void UpdateEmployee_ReportsTo_Pass() throws Exception {
        Office office = getDefaultOffice();

        createTestEmployee(4001, "Boss", "Manager", "boss@test.com", "CEO", null, office);
        createTestEmployee(4002, "John", "Doe", "john@test.com", "Dev", null, office);

        String updateJson = """
        {
            "manager": "/employees/4001"
        }
        """;

        mockMvc.perform(patch("/employees/4002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void UpdateEmployee_ReportsTo_Fail_Invalid() throws Exception {
        Office office = getDefaultOffice();
        createTestEmployee(4003, "John", "Doe", "john@test.com", "Dev", null, office);

        String updateJson = """
        {
            "manager": "/employees/9999"
        }
        """;

        mockMvc.perform(patch("/employees/4003")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().is4xxClientError());
    }

}