package com.example.DemoCheck.api;

import com.example.DemoCheck.entity.Office;
import com.example.DemoCheck.repository.OfficeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}