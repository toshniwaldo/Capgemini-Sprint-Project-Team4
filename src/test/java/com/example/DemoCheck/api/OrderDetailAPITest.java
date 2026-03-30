package com.example.DemoCheck.api;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderDetailAPITest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAllOrderDetails_Pagination() throws Exception {

        mockMvc.perform(get("/orderdetails")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())

            // Pagination checks
            .andExpect(jsonPath("$.page.size").value(20))
            .andExpect(jsonPath("$.page.number").value(0))
            .andExpect(jsonPath("$.page.totalElements").exists())
            .andExpect(jsonPath("$.page.totalPages").exists())

            // Data exists
            .andExpect(jsonPath("$._embedded.orderDetailses").exists())
            .andExpect(jsonPath("$._embedded.orderDetailses.length()").isNotEmpty());
    }

    @Test
    void testGetAllOrderDetails_SecondPage() throws Exception {

        mockMvc.perform(get("/orderdetails")
                .param("page", "1")
                .param("size", "20"))
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.page.number").value(1))
            .andExpect(jsonPath("$.page.size").value(20))
            .andExpect(jsonPath("$._embedded.orderDetailses").exists());
    }

    // @Test
    // void testGetAllOrderDetails_WithProjection() throws Exception {

    //     mockMvc.perform(get("/orderdetails")
    //             .param("page", "0")
    //             .param("size", "5")
    //             .param("projection", "orderDetailView"))
    //         .andExpect(status().isOk())

    //         .andExpect(jsonPath("$._embedded.orderDetailses[0].quantityOrdered").exists())
    //         .andExpect(jsonPath("$._embedded.orderDetailses[0].priceEach").exists())

    //         // nested projection fields
    //         .andExpect(jsonPath("$._embedded.orderDetailses[0].product.productName").exists())
    //         .andExpect(jsonPath("$._embedded.orderDetailses[0].order.orderNumber").exists());
    // }

    @Test
    void testGetOrderDetails_NoResults() throws Exception {

        mockMvc.perform(get("/orderdetails/search/findByProduct_ProductCode")
                .param("productCode", "INVALID_CODE")
                .param("page", "0")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.orderDetailses").isEmpty());
    }

    @Test
    void testGetOrderDetails_MissingParam() throws Exception {

        mockMvc.perform(get("/orderdetails/search/findByProduct_ProductCode"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.orderDetailses").isArray())
            .andExpect(jsonPath("$._embedded.orderDetailses.length()").value(0))
            .andExpect(jsonPath("$.page.totalElements").value(0))
            .andExpect(jsonPath("$.page.totalPages").value(0));
    }
}
