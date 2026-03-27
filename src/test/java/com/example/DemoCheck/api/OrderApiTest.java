package com.example.DemoCheck.api;

import com.example.DemoCheck.entity.Order;
import com.example.DemoCheck.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository repository;

    @Autowired
    private ObjectMapper objectMapper;


//    @Test
//    void testSaveOrder() throws Exception {
//
//        Order order = new Order();
//        order.setOrderNumber(10150);
//        order.setStatus("Processing");
//
//        mockMvc.perform(post("/orders")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(order)))
//                .andExpect(status().isCreated());
//    }
@Test
void testSaveOrder() throws Exception {
    String json = """
        {
            "orderNumber": 10150,
            "status": "Processing"
        }
        """;

    mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"));
}


    @Test
    void testGetOrderNotFound() throws Exception {

        mockMvc.perform(get("/orders/999999"))
                .andExpect(status().isNotFound());
    }
    @Test
    void testCreateOrder() throws Exception {

        String json = """
        {
            "orderNumber": 10150,
            "status": "Processing"
        }
        """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }
    @Test
    void testGetOrderById() throws Exception {

        mockMvc.perform(get("/orders/10150"))
                .andExpect(status().isOk());
    }
    @Test
    void testGetAllOrders() throws Exception {

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk());
    }
//    @Test
//    void testUpdateOrder() throws Exception {
//
//        String json = """
//        {
//            "status": "Shipped"
//        }
//        """;
//
//        mockMvc.perform(put("/orders/10150")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isOk());
//    }
    @Test
    void testDeleteOrder() throws Exception {

        mockMvc.perform(delete("/orders/10150"))
                .andExpect(status().isNoContent());
    }
    @Test
    void testValidationPass() throws Exception {

        String json = """
        {
            "orderNumber": 10151,
            "status": "Processing"
        }
        """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    // Failer Test Cases

//    @Test
//    void testCreateOrderFail() throws Exception {
//
//        String json = "{}";
//
//        mockMvc.perform(post("/orders")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isBadRequest());
//    }
    @Test
    void testGetInvalidId() throws Exception {

        mockMvc.perform(get("/orders/99999"))
                .andExpect(status().isNotFound());
    }
    @Test
    void testGetEmptyList() throws Exception {

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk());
    }
    @Test
    void testDeleteInvalid() throws Exception {

        mockMvc.perform(delete("/orders/99999"))
                .andExpect(status().isNotFound());
    }
//    @Test
//    void testUpdateInvalid() throws Exception {
//
//        String json = """
//        {
//            "status": "Cancelled"
//        }
//        """;
//
//        mockMvc.perform(put("/orders/99999")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isNotFound());
//    }
//    @Test
//    void testValidationFail() throws Exception {
//
//        String json = """
//        {
//            "status": null
//        }
//        """;
//
//        mockMvc.perform(post("/orders")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isBadRequest());
//    }
}