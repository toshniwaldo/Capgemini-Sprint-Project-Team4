package com.example.DemoCheck.api;

import com.example.DemoCheck.entity.Office;
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
        return new Office(
                officeCode,
                city,
                phone,
                addressLine1,
                addressLine2,
                state,
                country,
                postalCode,
                territory
        );
    }

    @Test
    void getAllOffices_shouldReturnData() throws Exception {
        officeRepository.saveAll(List.of(
                createOffice("T01", "Paris", "1111111111", "A1", "A2", "State1", "France", "75001", "EMEA"),
                createOffice("T02", "London", "2222222222", "B1", "B2", "State2", "UK", "EC1A", "EMEA")
        ));

        mockMvc.perform(get("/offices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.offices.length()", greaterThan(0)));
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

    //patch update pass case
    @Test
    void updatePhone_shouldUpdatePhone_whenOfficeExists() throws Exception{
        officeRepository.save(
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
                .andExpect(status().isNoContent());

        Office updated = officeRepository.findById("T93").orElseThrow();
        assertEquals("7777777777",updated.getPhone());
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
}