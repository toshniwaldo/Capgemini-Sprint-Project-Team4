package com.example.DemoCheck.controller;

import com.example.DemoCheck.repository.ProductLineRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductLinesAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductLineRepository repository;

    @Test
    void testGetAllProductLines() throws Exception {
        mockMvc.perform(get("/productlines"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.hal+json"))
                .andExpect(jsonPath("$._embedded.productLines.length()", greaterThan(0)));
    }

    @Test
    void testCreateProductLine() throws Exception {
        String json = """
            {
                "productLine": "Classic Cars",
                "textDescription": "desc"
            }
            """;

        mockMvc.perform(post("/productlines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }
}