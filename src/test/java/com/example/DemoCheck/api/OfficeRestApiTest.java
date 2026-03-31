package com.example.DemoCheck.api;

import com.example.DemoCheck.entity.Employee;
import com.example.DemoCheck.entity.Office;
import com.example.DemoCheck.repository.EmployeeRepository;
import com.example.DemoCheck.repository.OfficeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OfficeRestApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Office createOffice(
            String officeCode,
            String city,
            String phone,
            String addressLine1,
            String addressLine2,
            String state,
            String country,
            String postalCode,
            String territory
    ) {
        return Office.builder()
                .officeCode(officeCode)
                .city(city)
                .phone(phone)
                .addressLine1(addressLine1)
                .addressLine2(addressLine2)
                .state(state)
                .country(country)
                .postalCode(postalCode)
                .territory(territory)
                .build();
    }

    @Test
    void getAllOffices_shouldReturnData() throws Exception {
        officeRepository.saveAll(List.of(
                createOffice("T01", "Paris", "1111111111", "A1", "A2", "State1", "France", "75001", "EMEA"),
                createOffice("T02", "London", "2222222222", "B1", "B2", "State2", "UK", "EC1A", "EMEA")
        ));

        mockMvc.perform(get("/offices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.offices.length()").isNotEmpty())

                .andExpect(jsonPath("$._embedded.offices[0].city").exists())
                .andExpect(jsonPath("$._embedded.offices[0].phone").exists())
                .andExpect(jsonPath("$._embedded.offices[0].country").exists());
    }

    @Test
    void getOfficeByOfficeCode_shouldReturnOffice_whenOfficeCodeExists() throws Exception {
        officeRepository.save(
                createOffice("T11", "Tokyo", "3333333333", "C1", "C2", "State3", "Japan", "100-0001", "APAC")
        );

        mockMvc.perform(get("/offices/T11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Tokyo"))
                .andExpect(jsonPath("$.phone").value("3333333333"))
                .andExpect(jsonPath("$.country").value("Japan"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void getOfficeByOfficeCode_shouldReturnNotFound_whenOfficeCodeDoesNotExist() throws Exception {
        mockMvc.perform(get("/offices/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByCities_shouldReturnData_whenCitiesExist() throws Exception {
        officeRepository.saveAll(List.of(
                createOffice("T21", "Paris", "4444444444", "D1", "D2", "State4", "France", "75002", "EMEA"),
                createOffice("T22", "London", "5555555555", "E1", "E2", "State5", "UK", "EC1B", "EMEA"),
                createOffice("T23", "Tokyo", "6666666666", "F1", "F2", "State6", "Japan", "100-0002", "APAC")
        ));

        mockMvc.perform(get("/offices/search/by-cities")
                        .param("cities", "Paris,London"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.offices").exists());
    }

    @Test
    void findByCities_shouldReturnEmpty_whenCitiesDoNotExist() throws Exception {
        officeRepository.saveAll(List.of(
                createOffice("T31", "Paris", "7777777777", "G1", "G2", "State7", "France", "75003", "EMEA"),
                createOffice("T32", "London", "8888888888", "H1", "H2", "State8", "UK", "EC1C", "EMEA")
        ));

        mockMvc.perform(get("/offices/search/by-cities")
                        .param("cities", "Xcity,XY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.offices.length()").value(0));

    }

    //pass case
    @Test
    void addOffice_shouldCreateOffice_whenRequestIsValid() throws Exception{
        String body = """
                {
                  "officeCode": "T91",
                  "city": "Nagpur",
                  "phone": "9999999999",
                  "addressLine1": "Main Road",
                  "addressLine2": "Near Square",
                  "state": "MH",
                  "country": "India",
                  "postalCode": "440001",
                  "territory": "APAC"
                }
                """;

        mockMvc.perform(post("/offices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        assertTrue(officeRepository.existsById("T91"));
    }

    @Test
    void addOffice_shouldFail_whenCityIsBlank() throws Exception {
        String body = """
            {
              "officeCode": "T99",
              "city": "",
              "phone": "9999999999",
              "addressLine1": "Main Road",
              "country": "India",
              "postalCode": "440001",
              "territory": "APAC"
            }
            """;

        mockMvc.perform(post("/offices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    //patch update pass case
    @Test
    void updatePhone_shouldUpdatePhone_whenOfficeExists() throws Exception {
        officeRepository.saveAndFlush(
                createOffice("T93", "London", "2222222222", "B1", "B2", "S2", "UK", "EC1A", "EMEA")
        );

        String body = """
            {
              "phone": "7777777777"
            }
            """;

        mockMvc.perform(patch("/offices/T93")
                        .contentType("application/merge-patch+json")
                        .content(body))
                .andDo(print())
                .andExpect(status().isNoContent());

        Office updated = officeRepository.findById("T93").orElseThrow();
        assertEquals("7777777777", updated.getPhone());
    }
    //patch update fail case
    @Test
    void updatePhone_shouldFail_whenOfficeDoesNotExist() throws Exception{
        String body = """
                {
                  "phone": "7777777777"
                }
                """;

        mockMvc.perform(patch("/offices/INVALID91")
                .contentType("application/merge-patch+json")
                .content(body))
                .andExpect(status().isNotFound());
    }

    //full update pass case
    @Test
    void updateOffice_shouldUpdateOffice_whenOfficeExists() throws Exception{
        officeRepository.save(
                createOffice("T94", "Tokyo", "3333333333", "C1", "C2", "S3", "Japan", "100001", "APAC")
        );

        String body = """
                {
                  "city": "Tokyo Updated",
                  "phone": "6666666666",
                  "addressLine1": "Updated Line 1",
                  "addressLine2": "Updated Line 2",
                  "state": "TokyoState",
                  "country": "Japan",
                  "postalCode": "100002",
                  "territory": "APAC"
                }
                """;

        mockMvc.perform(put("/offices/T94")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isNoContent());

        Office updated = officeRepository.findById("T94").orElseThrow();
        assertEquals("Tokyo Updated",updated.getCity());
        assertEquals("6666666666",updated.getPhone());
        assertEquals("Updated Line 1",updated.getAddressLine1());
    }

    @Test
    void updateOffice_shouldCreateOffice_whenOfficeDoesNotExist() throws Exception {
        String body = """
                {
                  "city": "Brand New City",
                  "phone": "5555555555",
                  "addressLine1": "New 1",
                  "addressLine2": "New 2",
                  "state": "MH",
                  "country": "India",
                  "postalCode": "440003",
                  "territory": "APAC"
                }
                """;

        mockMvc.perform(put("/offices/T95")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        assertTrue(officeRepository.existsById("T95"));
    }


    //projection_pagination
    @Test
    void getAllOffices_shouldReturnProjectionFieldsWithPagination() throws Exception{
        officeRepository.save(createOffice(
                "P101", "London", "+44 20 7877 2041",
                "25 Old Broad Street", "Level 7", null,
                "UK", "EC2N 1HN", "EMEA"
        ));

        officeRepository.save(createOffice(
                "P102", "Tokyo", "+81 3 1234 5678",
                "Marunouchi 1-1", "Floor 5", null,
                "Japan", "100-0005", "APAC"
        ));

        officeRepository.save(createOffice(
                "P103", "Paris", "+33 1 2345 6789",
                "Champs Elysees", "Suite 2", null,
                "France", "75008", "EMEA"
        ));

        var request = get("/offices")
                .param("projection","officeList")
                .param("page","0")
                .param("size","2");

        var result = mockMvc.perform(request).andDo(print());

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.offices").exists())
                .andExpect(jsonPath("$._embedded.offices[0].officeCode").exists())
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").exists())
                .andExpect(jsonPath("$.page.totalPages").exists());
    }
    @Test
    void getAllOffices_shouldReturnEmptyPage_whenPageOutOfRange() throws Exception{
        officeRepository.save(createOffice("P101", "London", "+44 20 7877 2041",
                "25 Old Broad Street", "Level 7", null, "UK", "EC2N 1HN", "EMEA"));

        officeRepository.save(createOffice("P102", "Tokyo", "+81 3 1234 5678",
                "Marunouchi 1-1", "Floor 5", null, "Japan", "100-0005", "APAC"));

        var request = get("/offices")
                .param("projection","officeList")
                .param("page","5")
                .param("size","2");

        var result = mockMvc.perform(request).andDo(print());

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.offices.length()").value(0))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(5));

    }

    //pass
    @Test
    void filterByCityWithPagination_shouldReturnMatchingOffices() throws Exception {
        officeRepository.save(createOffice("C101", "TestCity_X", "1111111111",
                "A1", "A2", "State1", "India", "400001", "APAC"));
        officeRepository.save(createOffice("C102", "TestCity_X", "2222222222",
                "B1", "B2", "State2", "India", "400002", "APAC"));
        officeRepository.save(createOffice("C103", "OtherCity_X", "3333333333",
                "C1", "C2", "State3", "India", "400003", "EMEA"));

        mockMvc.perform(get("/offices/search/by-city")
                        .param("city", "TestCity_X")
                        .param("projection","officeList")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.offices.length()").value(2))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0));
    }
    //fail
    @Test
    void filterByCityWithPagination_shouldReturnEmpty_whenCityDoesNotExist() throws Exception {
        officeRepository.save(createOffice("NC101", "RealCity_X", "1111111111",
                "A1", "A2", "State1", "India", "400001", "APAC"));

        mockMvc.perform(get("/offices/search/by-city")
                        .param("city", "NoSuchCity_999")
                        .param("projection","officeList")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0));
    }

    //pass
    @Test
    void filterByStateWithPagination_shouldReturnMatchingOffices() throws Exception {
        officeRepository.save(createOffice("S101", "City1", "1111111111",
                "A1", "A2", "TestState_X", "India", "400001", "APAC"));
        officeRepository.save(createOffice("S102", "City2", "2222222222",
                "B1", "B2", "TestState_X", "India", "400002", "APAC"));
        officeRepository.save(createOffice("S103", "City3", "3333333333",
                "C1", "C2", "OtherState_X", "India", "400003", "EMEA"));

        mockMvc.perform(get("/offices/search/by-state")
                        .param("state", "TestState_X")
                        .param("projection","officeList")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.offices").exists())
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0));
    }
    //fail
    @Test
    void filterByStateWithPagination_shouldReturnEmpty_whenStateDoesNotExist() throws Exception {
        officeRepository.save(createOffice("NS101", "City1", "1111111111",
                "A1", "A2", "RealState_X", "India", "400001", "APAC"));

        mockMvc.perform(get("/offices/search/by-state")
                        .param("state", "NoSuchState_999")
                        .param("projection","officeList")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0));
    }

    //Pass
    @Test
    void filterByCountryWithPagination_shouldReturnMatchingOffices() throws Exception {
        officeRepository.save(createOffice("CO101", "City1", "1111111111",
                "A1", "A2", "State1", "TestCountry_X", "400001", "APAC"));
        officeRepository.save(createOffice("CO102", "City2", "2222222222",
                "B1", "B2", "TestCountry_X", "400002", "EMEA","EMEA"));
        officeRepository.save(createOffice("CO103", "City3", "3333333333",
                "C1", "C2", "State3", "OtherCountry_X", "400003", "EMEA"));

        mockMvc.perform(get("/offices/search/by-country")
                        .param("country", "TestCountry_X")
                        .param("projection","officeList")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.offices").exists())
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0));
    }
    //fail
    @Test
    void filterByCountryWithPagination_shouldReturnEmpty_whenCountryDoesNotExist() throws Exception {
        officeRepository.save(createOffice("NCO101", "City1", "1111111111",
                "A1", "A2", "State1", "RealCountry_X", "400001", "APAC"));

        mockMvc.perform(get("/offices/search/by-country")
                        .param("country", "NoSuchCountry_999")
                        .param("projection","officeList")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0));
    }

    //pass
    @Test
    void filterByTerritoryWithPagination_shouldReturnMatchingOffices() throws Exception {
        officeRepository.save(createOffice("TT901", "City1", "1111111111",
                "A1", "A2", "State1", "India", "400001", "TEST_TER_X"));
        officeRepository.save(createOffice("TT902", "City2", "2222222222",
                "B1", "B2", "State2", "India", "400002", "TEST_TER_X"));
        officeRepository.save(createOffice("TT903", "City3", "3333333333",
                "C1", "C2", "State3", "India", "400003", "OTHERTERX"));

        mockMvc.perform(get("/offices/search/by-territory")
                        .param("territory", "TEST_TER_X")
                        .param("projection","officeList")
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk());
    }
    //fail
    @Test
    void filterByTerritoryWithPagination_shouldReturnEmpty_whenTerritoryDoesNotExist() throws Exception {
        officeRepository.save(createOffice("NT101", "City1", "1111111111",
                "A1", "A2", "State1", "India", "400001", "REALTER_X"));

        mockMvc.perform(get("/offices/search/by-territory")
                        .param("territory", "NO_TER_9")
                        .param("projection","officeList")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0));
    }

    //3rd page
    private Employee createEmployee(
            Integer employeeNumber,
            String firstName,
            String lastName,
            String extension,
            String email,
            String jobTitle,
            Office office
    ) {
        Employee employee = new Employee();
        employee.setEmployeeNumber(employeeNumber);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setExtension(extension);
        employee.setEmail(email);
        employee.setJobTitle(jobTitle);
        employee.setOffice(office);
        return employee;
    }
    @Test
    void getEmployeesByOfficeCode_shouldReturnEmployeesOfSelectedOffice() throws Exception{
        Office office1 = officeRepository.save(
                createOffice("10", "London", "1111111111", "A1", "A2", null, "UK", "EC2N", "EMEA")
        );
        Office office2 = officeRepository.save(
                createOffice("12", "Tokyo", "2222222222", "B1", "B2", null, "Japan", "100001", "APAC")
        );

        employeeRepository.save(createEmployee(2001, "John", "Doe", "x101", "john@example.com", "Sales Rep", office1));
        employeeRepository.save(createEmployee(2002, "Jane", "Smith", "x102", "jane@example.com", "Manager", office1));
        employeeRepository.save(createEmployee(2003, "Taro", "Yamada", "x201", "taro@example.com", "Clerk", office2));

        mockMvc.perform(get("/employees/search/by-office-code")
                .param("officeCode","10")
                .param("projection","employeeList")
                .param("page","0")
                .param("size","6"))
                .andDo(print())

                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.employees").exists())
                .andExpect(jsonPath("$._embedded.employees[0].employeeNumber").exists())
                .andExpect(jsonPath("$._embedded.employees[0].fullName").exists())
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(6));
    }
}