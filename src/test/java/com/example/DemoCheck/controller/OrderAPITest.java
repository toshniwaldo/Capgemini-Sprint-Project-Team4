package com.example.DemoCheck.controller;

import com.example.DemoCheck.entity.Orders;
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
public class OrderAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository repository;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void testSaveOrder() throws Exception {

        Orders order = new Orders();
        order.setOrderNumber(10150);
        order.setStatus("Processing");

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated());

    }
    @Test
    void testGetOrderNotFound() throws Exception {

        mockMvc.perform(get("/orders/999999"))
                .andExpect(status().isNotFound());
    }




}