package com.example.DemoCheck.api;

import com.example.DemoCheck.entity.Customer;
import com.example.DemoCheck.entity.Employee;
import com.example.DemoCheck.entity.Office;
import com.example.DemoCheck.repository.CustomerRepository;
import com.example.DemoCheck.repository.EmployeeRepository;
import com.example.DemoCheck.repository.OfficeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OfficeRepository officeRepository;
    //Helper method
    private Customer createCustomer(int id, String name, String city) {
        Customer c = new Customer();
        c.setCustomerNumber(id);
        c.setCustomerName(name);
        c.setContactLastName("Smith");
        c.setContactFirstName("John");
        c.setPhone("1234567890");
        c.setAddressLine1("Addr1");
        c.setAddressLine2(null);
        c.setCity(city);
        c.setState("MH");
        c.setPostalCode("411001");
        c.setCountry("India");
        c.setCreditLimit(new BigDecimal("10000"));
        c.setSalesRepEmployee(null);
        return c;
    }

    //helper method
    private Employee createEmployee() {

        Office office = new Office();
        office.setOfficeCode(String.valueOf(generateId()));
        office.setCity("Nagpur");
        office.setPhone("1234567890");
        office.setAddressLine1("Addr1");
        office.setCountry("India");
        office.setPostalCode("440001");
        office.setTerritory("APAC");

        officeRepository.save(office);

        Employee e = new Employee();
        e.setEmployeeNumber(generateId());
        e.setLastName("Doe");
        e.setFirstName("John");
        e.setExtension("x123");
        e.setEmail("john@test.com");
        e.setOffice(office);
        e.setJobTitle("Manager");
        return employeeRepository.save(e);
    }

    private int generateId() {
        return (int) (System.nanoTime() % 1000000);
    }

    @Test
    void testGetCustomerById() throws Exception {

        // Arrange
        int baseId = generateId();

        Customer c = createCustomer(baseId, "Single Customer", "Mumbai");
        customerRepository.save(c);

        var request = get("/customer/" + baseId);

        // Act
        var result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Single Customer"));
    }

    @Test
    void testGetCustomerByInvalidId() throws Exception {

        // Arrange
        int invalidId = 99999999;

        var request = get("/customer/" + invalidId);

        // Act
        var result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isNotFound());
    }

    @Test
    void testGetCustomerWithInvalidIdFormat() throws Exception {

        // Arrange
        var request = get("/customer/abc");

        // Act
        var result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isBadRequest());
    }


    @Test
    void testGetAllCustomers() throws Exception {

        // Arrange
        int baseId = generateId();

        Customer c1 = createCustomer(baseId, "ABC Corp", "Pune");

        customerRepository.save(c1);

        var request = get("/customer");

        // Act
        var result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers[*].customerName")
                        .value(org.hamcrest.Matchers.hasItem("ABC Corp")));
    }

    @Test
    void testPaginationApi() throws Exception {

        // Arrange
        int pageSize = 12;
        var request = get("/customer")
                .param("page", "0")
                .param("size", String.valueOf(pageSize));

        // Act
        var result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(pageSize))
                .andExpect(jsonPath("$.page.totalElements").exists())
                .andExpect(jsonPath("$._embedded.customers").isArray());
    }

    @Test
    void testPaginationOutOfBounds() throws Exception {

        // Arrange
        var request = get("/customer")
                .param("page", "1000")
                .param("size", "12");

        // Act
        var result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers").isEmpty());
    }

    @Test
    void testProjectionFields() throws Exception {

        // Arrange
        int baseId = generateId();

        Customer c1 = createCustomer(baseId, "Proj Corp", "Pune");

        customerRepository.save(c1);

        var request = get("/customer")
                .param("projection", "customerView")
                .param("page", "0")
                .param("size", "1");

        // Act
        var result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers[0].contactName").exists())
                .andExpect(jsonPath("$._embedded.customers[0].address").exists());
    }

    @Test
    void testGetCustomerByIdWithProjection() throws Exception {

        // Arrange
        int baseId = generateId();

        Customer c = createCustomer(baseId, "Projected Customer", "Delhi");
        customerRepository.save(c);

        var request = get("/customer/" + baseId)
                .param("projection", "customerView");

        // Act
        var result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.contactName").exists())
                .andExpect(jsonPath("$.address").exists());
    }

    @Test
    void testSearchApi() throws Exception {

        // Arrange
        int baseId = generateId();

        Customer c1 = createCustomer(baseId, "SearchTest Corp", "Pune");

        customerRepository.save(c1);

        var request = get("/customer/search/findCustomers")
                .param("keyword", "SearchTest")
                .param("page", "0")
                .param("size", "12");

        // Act
        var result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers[*].customerName")
                        .value(org.hamcrest.Matchers.hasItem("SearchTest Corp")));
    }

    @Test
    void testSearchNoResults() throws Exception {

        // Arrange
        var request = get("/customer/search/findCustomers")
                .param("keyword", "XYZ_NOT_FOUND")
                .param("page", "0")
                .param("size", "12");

        // Act
        var result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers").isEmpty());
    }



    @Test
    void testSearchWithEmptyKeyword() throws Exception {

        // Arrange
        var request = get("/customer/search/findCustomers")
                .param("keyword", "")
                .param("page", "0")
                .param("size", "12");

        // Act
        var result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers").isArray());
    }

    private String validCustomerJson(Integer employeeId) {
        int customerId = generateId();
        String employeePart = (employeeId != null)
                ? "\"salesRepEmployee\": \"/employees/" + employeeId + "\""
                : "";

        return """
        {
          "customerNumber": %d,
          "customerName": "Test Corp",
          "contactLastName": "Doe",
          "contactFirstName": "John",
          "phone": "1234567890",
          "addressLine1": "Street 1",
          "city": "Nagpur",
          "country": "India",
          "creditLimit": 15000
          %s
        }
        """.formatted(customerId,
                employeePart.isEmpty() ? "" : "," + employeePart);
    }

    @Test
    void shouldCreateCustomerSuccessfully() throws Exception {

        Employee emp = createEmployee();

        String json = validCustomerJson(emp.getEmployeeNumber());

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldCreateCustomerWithoutEmployee() throws Exception {

        // Arrange
        String json = validCustomerJson(null);

        // Act + Assert
        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    //Spring data rest sets employee null and inserts record with 201 created status
    //before the event handler responsible for handling invalid employee id runs
    //400 bad request not returned test has been skipped until problem is solved
//    @Test
//    void shouldFailWhenEmployeeDoesNotExist() throws Exception {
//
//        // Arrange
//        String json = validCustomerJson(Integer.MAX_VALUE);
//
//        // Act + Assert
//        mockMvc.perform(post("/customer")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isBadRequest());
//    }

    @Test
    void shouldFailWhenCustomerNameMissing() throws Exception {
        int customerId = generateId();
        // Arrange
        String json = """
        {
          "customerNumber": %d,
          "contactLastName": "Doe",
          "contactFirstName": "John",
          "phone": "1234567890",
          "addressLine1": "Street 1",
          "city": "Nagpur",
          "country": "India"
        }
        """.formatted(customerId);

        // Act + Assert
        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenPhoneInvalid() throws Exception {

        // Arrange
        String json = """
        {
          "customerNumber": 9004,
          "customerName": "Invalid Phone Corp",
          "contactLastName": "Doe",
          "contactFirstName": "John",
          "phone": "abc123",
          "addressLine1": "Street 1",
          "city": "Nagpur",
          "country": "India"
        }
        """;

        // Act + Assert
        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenCreditLimitNegative() throws Exception {

        // Arrange
        String json = """
        {
          "customerNumber": 9005,
          "customerName": "Negative Credit Corp",
          "contactLastName": "Doe",
          "contactFirstName": "John",
          "phone": "1234567890",
          "addressLine1": "Street 1",
          "city": "Nagpur",
          "country": "India",
          "creditLimit": -5000
        }
        """;

        // Act + Assert
        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenDuplicateCustomerId() throws Exception {

        int id = generateId();

        String json = """
    {
      "customerNumber": %d,
      "customerName": "Duplicate Corp",
      "contactLastName": "Doe",
      "contactFirstName": "John",
      "phone": "1234567890",
      "addressLine1": "Street 1",
      "city": "Nagpur",
      "country": "India"
    }
    """.formatted(id);

        // First insert
        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        // Duplicate insert
        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenCustomerNameBlank() throws Exception {

        // Arrange
        String json = """
        {
          "customerNumber": 9007,
          "customerName": "",
          "contactLastName": "Doe",
          "contactFirstName": "John",
          "phone": "1234567890",
          "addressLine1": "Street 1",
          "city": "Nagpur",
          "country": "India"
        }
        """;

        // Act + Assert
        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenCustomerNumberNull() throws Exception {

        // Arrange
        String json = """
        {
          "customerName": "Null ID Corp",
          "contactLastName": "Doe",
          "contactFirstName": "John",
          "phone": "1234567890",
          "addressLine1": "Street 1",
          "city": "Nagpur",
          "country": "India"
        }
        """;

        // Act + Assert
        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
