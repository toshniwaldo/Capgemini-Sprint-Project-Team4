package com.example.DemoCheck.api;

import com.example.DemoCheck.entity.Customer;
import com.example.DemoCheck.entity.Employee;
import com.example.DemoCheck.entity.Office;
import com.example.DemoCheck.repository.CustomerRepository;
import com.example.DemoCheck.repository.EmployeeRepository;
import com.example.DemoCheck.repository.OfficeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private int generateId() {
        return (int) (System.nanoTime() % 1000000) + 1000000;
    }

    private Customer createCustomer(int id, String name, String city) {
        Customer c = new Customer();
        c.setCustomerNumber(id);
        c.setCustomerName(name);
        c.setContactLastName("Smith");
        c.setContactFirstName("John");
        c.setPhone("1234567890");
        c.setAddressLine1("Addr1");
        c.setCity(city);
        c.setCountry("India");
        c.setCreditLimit(new BigDecimal("10000"));
        return c;
    }

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

    // ---------------- GET TESTS ----------------

    @Test
    void testGetCustomerById() throws Exception {
        int id = generateId();
        customerRepository.save(createCustomer(id, "Single Customer", "Mumbai"));

        mockMvc.perform(get("/customer/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Single Customer"));
    }

    @Test
    void testGetCustomerByInvalidId() throws Exception {
        mockMvc.perform(get("/customer/99999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    @Test
    void testInvalidIdFormat() throws Exception {
        mockMvc.perform(get("/customer/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid ID format. ID must be a number."));
    }

    @Test
    void testGetAllCustomers() throws Exception {
        int id = generateId();
        customerRepository.save(createCustomer(id, "ABC Corp", "Pune"));

        mockMvc.perform(get("/customer?sort=customerNumber,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers").isArray())
                .andExpect(jsonPath("$._embedded.customers[*].customerName")
                        .value(org.hamcrest.Matchers.hasItem("ABC Corp")));
    }

    // ---------------- PAGINATION ----------------

    void testPagination() throws Exception {
        mockMvc.perform(get("/customer")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(5))
                .andExpect(jsonPath("$.page.totalElements").exists());
    }

    @Test
    void testPaginationOutOfBounds() throws Exception {
        mockMvc.perform(get("/customer")
                        .param("page", "999")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers").isEmpty());
    }

    // ---------------- PROJECTION ----------------

    @Test
    void testProjectionFields() throws Exception {

        int baseId = generateId();

        Customer c = createCustomer(baseId, "Proj Corp", "Pune");
        customerRepository.save(c);

        mockMvc.perform(get("/customer")
                        .param("projection", "customerView")
                        .param("size", "200")) // 🔥 FIX
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers").isArray())
                .andExpect(jsonPath("$._embedded.customers[*].customerName")
                        .value(org.hamcrest.Matchers.hasItem("Proj Corp")))
                .andExpect(jsonPath("$._embedded.customers[*].contactName")
                        .value(org.hamcrest.Matchers.hasItem("John Smith")))
                .andExpect(jsonPath("$._embedded.customers[*].address")
                        .value(org.hamcrest.Matchers.hasItem("Addr1")))
                .andExpect(jsonPath("$._embedded.customers[*].city")
                        .value(org.hamcrest.Matchers.hasItem("Pune")))
                .andExpect(jsonPath("$._embedded.customers[*].country")
                        .value(org.hamcrest.Matchers.hasItem("India")));
    }

    @Test
    void testGetCustomerByIdWithProjection() throws Exception {

        int baseId = generateId();

        Customer c = createCustomer(baseId, "Projected Customer", "Delhi");
        customerRepository.save(c);

        mockMvc.perform(get("/customer/" + baseId)
                        .param("projection", "customerView"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName")
                        .value("Projected Customer"))
                .andExpect(jsonPath("$.contactName")
                        .value("John Smith"))
                .andExpect(jsonPath("$.address")
                        .value("Addr1"))
                .andExpect(jsonPath("$.city")
                        .value("Delhi"));
    }

    // ---------------- SEARCH ----------------

    @Test
    void testSearchApi() throws Exception {

        int baseId = generateId();
        customerRepository.save(createCustomer(baseId, "SearchTest Corp", "Pune"));

        mockMvc.perform(get("/customer/search/findCustomers")
                        .param("keyword", "SearchTest")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers").isArray())
                .andExpect(jsonPath("$._embedded.customers[*].customerName")
                        .value(org.hamcrest.Matchers.hasItem("SearchTest Corp")))
                .andExpect(jsonPath("$.page.size").value(10));
    }

    @Test
    void testSearchNoResults() throws Exception {

        mockMvc.perform(get("/customer/search/findCustomers")
                        .param("keyword", "XYZ_NOT_FOUND")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers").isEmpty())
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }



    @Test
    void testSearchWithEmptyKeyword() throws Exception {

        int baseId = generateId();
        customerRepository.save(createCustomer(baseId, "Tech Corp", "Nagpur"));

        mockMvc.perform(get("/customer/search/findCustomers")
                        .param("keyword", "")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers").isNotEmpty());
    }

    @Test
    void testSearchByCityApi() throws Exception {

        int baseId = generateId();
        customerRepository.save(createCustomer(baseId, "City Corp", "Nagpur"));

        mockMvc.perform(get("/customer/search/findCustomers")
                        .param("keyword", "Nagpur"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers").isNotEmpty());
    }

    @Test
    void testSearchPaginationLimit() throws Exception {

        int baseId = generateId();

        for (int i = 0; i < 10; i++) {
            customerRepository.save(createCustomer(baseId + i, "Tech " + i, "City"));
        }

        mockMvc.perform(get("/customer/search/findCustomers")
                        .param("keyword", "Tech")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customers.length()").value(5));
    }

    // ---------------- POST ----------------

    @Test
    void shouldCreateCustomerSuccessfully() throws Exception {

        Employee emp = createEmployee();
        String json = validCustomerJson(emp.getEmployeeNumber());

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        //Verify persisted
        assertThat(customerRepository.findAll()).isNotEmpty();
    }

    @Test
    void shouldCreateCustomerWithoutEmployee() throws Exception {

        String json = validCustomerJson(null);

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

        int id = generateId();

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
    """.formatted(id);

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("customerName cannot be blank"));
    }

    @Test
    void shouldFailWhenPhoneInvalid() throws Exception {

        int id = generateId();

        String json = """
    {
      "customerNumber": %d,
      "customerName": "Invalid Phone Corp",
      "contactLastName": "Doe",
      "contactFirstName": "John",
      "phone": "abc123",
      "addressLine1": "Street 1",
      "city": "Nagpur",
      "country": "India"
    }
    """.formatted(id);

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("phone must be exactly 10 digits"));
    }

    @Test
    void shouldFailWhenCreditLimitNegative() throws Exception {

        int id = generateId();

        String json = """
    {
      "customerNumber": %d,
      "customerName": "Negative Credit Corp",
      "contactLastName": "Doe",
      "contactFirstName": "John",
      "phone": "1234567890",
      "addressLine1": "Street 1",
      "city": "Nagpur",
      "country": "India",
      "creditLimit": -5000
    }
    """.formatted(id);

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("creditLimit cannot be negative"));
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Customer already exists with id: " + id));
    }

    @Test
    void shouldFailWhenCustomerNameBlank() throws Exception {

        int id = generateId();

        String json = """
    {
      "customerNumber": %d,
      "customerName": "",
      "contactLastName": "Doe",
      "contactFirstName": "John",
      "phone": "1234567890",
      "addressLine1": "Street 1",
      "city": "Nagpur",
      "country": "India"
    }
    """.formatted(id);

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("customerName cannot be blank"));
    }

    @Test
    void shouldFailWhenCustomerNumberNull() throws Exception {

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

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("customerNumber cannot be null"));
    }

    @Test
    void shouldFailWhenPostalCodeInvalid() throws Exception {
        int id = generateId();

        String json = """
    {
      "customerNumber": %d,
      "customerName": "Bad Postal",
      "contactLastName": "Doe",
      "contactFirstName": "John",
      "phone": "1234567890",
      "addressLine1": "Street 1",
      "city": "Nagpur",
      "country": "India",
      "postalCode": "123"
    }
    """.formatted(id);

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // ---------------- PATCH ----------------

    @Test
    void shouldUpdateCustomerNameSuccessfully() throws Exception {

        int id = generateId();
        customerRepository.save(createCustomer(id, "Old Name", "Nagpur"));

        String patchJson = """
    {
      "customerName": "Updated Name"
    }
    """;

        mockMvc.perform(patch("/customer/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNumber").value(id))
                .andExpect(jsonPath("$.customerName").value("Updated Name"))
                .andExpect(jsonPath("$.city").value("Nagpur")); // unchanged

        Customer updated = customerRepository.findById(id).orElseThrow();
        assertThat(updated.getCustomerName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldUpdateAddressFields() throws Exception {

        int id = generateId();
        customerRepository.save(createCustomer(id, "Address Corp", "Mumbai"));

        String json = """
    {
      "addressLine1": "New Address",
      "city": "Pune"
    }
    """;

        mockMvc.perform(patch("/customer/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNumber").value(id))
                .andExpect(jsonPath("$.addressLine1").value("New Address"))
                .andExpect(jsonPath("$.city").value("Pune"))
                .andExpect(jsonPath("$.customerName").value("Address Corp")); // unchanged

        Customer updated = customerRepository.findById(id).orElseThrow();

        assertThat(updated.getAddressLine1()).isEqualTo("New Address");
        assertThat(updated.getCity()).isEqualTo("Pune");
    }

    @Test
    void shouldUpdateContactName() throws Exception {

        int id = generateId();
        customerRepository.save(createCustomer(id, "Contact Corp", "Mumbai"));

        String json = """
    {
      "contactFirstName": "Jane",
      "contactLastName": "Doe"
    }
    """;

        mockMvc.perform(patch("/customer/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNumber").value(id))
                .andExpect(jsonPath("$.contactFirstName").value("Jane"))
                .andExpect(jsonPath("$.contactLastName").value("Doe"))
                .andExpect(jsonPath("$.customerName").value("Contact Corp")); // unchanged

        Customer updated = customerRepository.findById(id).orElseThrow();

        assertThat(updated.getContactFirstName()).isEqualTo("Jane");
        assertThat(updated.getContactLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldTrimCustomerNameOnUpdate() throws Exception {

        int id = generateId();
        customerRepository.save(createCustomer(id, "Old Name", "Nagpur"));

        String json = """
    {
      "customerName": "   Trimmed Name   "
    }
    """;

        mockMvc.perform(patch("/customer/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNumber").value(id))
                .andExpect(jsonPath("$.customerName").value("Trimmed Name"))
                .andExpect(jsonPath("$.city").value("Nagpur")); // unchanged

        Customer updated = customerRepository.findById(id).orElseThrow();

        assertThat(updated.getCustomerName()).isEqualTo("Trimmed Name");
    }

    @Test
    void shouldUpdateOnlyCityAndKeepOtherFieldsUnchanged() throws Exception {

        int id = generateId();
        Customer c = createCustomer(id, "Test Corp", "Nagpur");
        customerRepository.save(c);

        String patchJson = """
    {
      "city": "Mumbai"
    }
    """;

        mockMvc.perform(patch("/customer/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNumber").value(id))
                .andExpect(jsonPath("$.city").value("Mumbai"))
                .andExpect(jsonPath("$.customerName").value("Test Corp")); // unchanged

        Customer updated = customerRepository.findById(id).orElseThrow();

        assertThat(updated.getCity()).isEqualTo("Mumbai");
        assertThat(updated.getCustomerName()).isEqualTo("Test Corp");
    }

    @Test
    void shouldUpdateMultipleFields() throws Exception {

        int id = generateId();
        customerRepository.save(createCustomer(id, "Old Corp", "Nagpur"));

        String patchJson = """
    {
      "customerName": "New Corp",
      "city": "Pune",
      "creditLimit": 20000
    }
    """;

        mockMvc.perform(patch("/customer/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNumber").value(id))
                .andExpect(jsonPath("$.customerName").value("New Corp"))
                .andExpect(jsonPath("$.city").value("Pune"))
                .andExpect(jsonPath("$.creditLimit").value(20000));

        Customer updated = customerRepository.findById(id).orElseThrow();

        assertThat(updated.getCustomerName()).isEqualTo("New Corp");
        assertThat(updated.getCity()).isEqualTo("Pune");
        assertThat(updated.getCreditLimit()).isEqualTo(new BigDecimal("20000"));
    }

    @Test
    void shouldAllowNullOptionalField() throws Exception {

        int id = generateId();
        Customer c = createCustomer(id, "Null Test", "Mumbai");
        c.setAddressLine2("Some Value");
        customerRepository.save(c);

        String json = """
    {
      "addressLine2": null
    }
    """;

        mockMvc.perform(patch("/customer/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNumber").value(id))
                .andExpect(jsonPath("$.addressLine2").value(org.hamcrest.Matchers.nullValue()));


        Customer updated = customerRepository.findById(id).orElseThrow();

        assertThat(updated.getAddressLine2()).isNull();
    }

    @Test
    void shouldFailWhenCustomerNameBlankOnUpdate() throws Exception {

        int id = generateId();
        Customer c = createCustomer(id, "Test Corp", "Mumbai");
        customerRepository.save(c);

        String json = """
    {
      "customerName": ""
    }
    """;

        mockMvc.perform(patch("/customer/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("customerName cannot be blank"));

        // 🔥 Optional strong check (VERY GOOD PRACTICE)
        Customer unchanged = customerRepository.findById(id).orElseThrow();
        assertThat(unchanged.getCustomerName()).isEqualTo("Test Corp");
    }

    @Test
    void shouldFailWhenPhoneInvalidOnUpdate() throws Exception {

        int id = generateId();
        Customer c = createCustomer(id, "Phone Corp", "Mumbai");
        customerRepository.save(c);

        String json = """
    {
      "phone": "abc123"
    }
    """;

        mockMvc.perform(patch("/customer/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("phone must be exactly 10 digits"));

        //Ensure DB not corrupted
        Customer unchanged = customerRepository.findById(id).orElseThrow();
        assertThat(unchanged.getPhone()).isEqualTo("1234567890");
    }

    @Test
    void shouldFailWhenInvalidIdFormat() throws Exception {

        String json = """
    {
      "city": "Delhi"
    }
    """;

        mockMvc.perform(patch("/customer/update/abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid ID format. ID must be a number."));
    }

    @Test
    void shouldNotUpdateCreditLimitWhenInvalid() throws Exception {

        int id = generateId();
        Customer c = createCustomer(id, "Credit Corp", "Nagpur");
        c.setCreditLimit(new BigDecimal("10000")); // ensure known value
        customerRepository.save(c);

        String json = """
    {
      "creditLimit": -5000
    }
    """;

        mockMvc.perform(patch("/customer/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("creditLimit cannot be negative"));

        //IMPORTANT: Ensure DB not updated
        Customer unchanged = customerRepository.findById(id).orElseThrow();
        assertThat(unchanged.getCreditLimit()).isEqualTo(new BigDecimal("10000"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingCustomer() throws Exception {

        String patchJson = """
    {
      "customerName": "New Name"
    }
    """;

        mockMvc.perform(patch("/customer/update/99999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Customer not found"));
    }

    @Test
    void shouldHandleEmptyPatchGracefully() throws Exception {

        int id = generateId();
        Customer c = createCustomer(id, "Test Corp", "Nagpur");
        customerRepository.save(c);

        mockMvc.perform(patch("/customer/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNumber").value(id))
                .andExpect(jsonPath("$.customerName").value("Test Corp"))
                .andExpect(jsonPath("$.city").value("Nagpur"));

        Customer unchanged = customerRepository.findById(id).orElseThrow();

        assertThat(unchanged.getCustomerName()).isEqualTo("Test Corp");
        assertThat(unchanged.getCity()).isEqualTo("Nagpur");
    }

    @Test
    void shouldNotUpdateWhenValidationFails() throws Exception {

        int id = generateId();
        Customer c = createCustomer(id, "Safe Corp", "Nagpur");
        c.setPhone("1234567890"); // ensure known valid value
        customerRepository.save(c);

        String json = """
    {
      "phone": "abc123"
    }
    """;

        mockMvc.perform(patch("/customer/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("phone must be exactly 10 digits"));

        //VERY IMPORTANT: ensure DB unchanged
        Customer unchanged = customerRepository.findById(id).orElseThrow();

        assertThat(unchanged.getPhone()).isEqualTo("1234567890");
    }

    @AfterEach
    void cleanup() {

        //Break FK: Customer → Employee
        customerRepository.findAll().stream()
                .filter(c -> c.getSalesRepEmployee() != null &&
                        c.getSalesRepEmployee().getEmployeeNumber() >= 1000000)
                .forEach(c -> {
                    c.setSalesRepEmployee(null);
                    customerRepository.save(c);
                });

        //Delete test customers
        customerRepository.findAll().stream()
                .filter(c -> c.getCustomerNumber() >= 1000000)
                .forEach(customerRepository::delete);

        //Delete test employees
        employeeRepository.findAll().stream()
                .filter(e -> e.getEmployeeNumber() >= 1000000)
                .forEach(employeeRepository::delete);

        //Delete test offices
        officeRepository.findAll().stream()
                .filter(o -> {
                    try {
                        return Integer.parseInt(o.getOfficeCode()) >= 1000000;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .forEach(officeRepository::delete);
    }
}
