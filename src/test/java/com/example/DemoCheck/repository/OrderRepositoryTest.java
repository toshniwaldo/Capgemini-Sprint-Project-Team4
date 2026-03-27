package com.example.DemoCheck.repository;

import com.example.DemoCheck.entity.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;
//    Order order = new Order();



    // TestCase Which will going to pass


    @Test
    void shouldSaveOrder() {

        Order order = new Order();
        order.setOrderNumber(10100);
        order.setOrderDate(LocalDate.now());
        order.setRequiredDate(LocalDate.now().plusDays(5));
        order.setStatus("In Process");
        order.setComments("Test Order");

        Order savedOrder = orderRepository.save(order);

        assertNotNull(savedOrder);
        assertEquals(10100, savedOrder.getOrderNumber());
    }
    @Test
    void shouldFindById() {
        Order order = new Order();
        order.setOrderNumber(10102);
        orderRepository.save(order);

        Optional<Order> found = orderRepository.findById(10102);

        assertTrue(found.isPresent());
    }
    @Test
    void shouldFindAllOrders() {
        List<Order> orders = orderRepository.findAll();
        assertNotNull(orders);
    }
    @Test
    void shouldDeleteOrder() {
        Order order = new Order();
        order.setOrderNumber(10103);
        orderRepository.save(order);

        orderRepository.deleteById(10103);

        assertFalse(orderRepository.findById(10103).isPresent());
    }
    @Test
    void shouldUpdateOrder() {
        Order order = new Order();
        order.setOrderNumber(10104);
        order.setStatus("Processing");

        orderRepository.save(order);

        order.setStatus("Shipped");
        orderRepository.save(order);

        Order updated = orderRepository.findById(10104).get();

        assertEquals("Shipped", updated.getStatus());
    }
//    @Test
//    void shouldCheckExists() {
//        Order order = new Order();
//        order.setOrderNumber(10105);
//        orderRepository.save(order);
//
//        assertTrue(orderRepository.existsById(10105));
//    }

    // Fail Test Cases

    @Test
    void shouldFailSaveNull() {
        assertThrows(Exception.class, () -> {
            orderRepository.save(null);
        });
    }
    @Test
    void shouldReturnEmptyForInvalidId() {
        Optional<Order> order = orderRepository.findById(99999);
        assertFalse(order.isPresent());
    }
    @Test
    void shouldFailDeleteInvalidId() {
        orderRepository.deleteById(99999);
    }

    @Test
    void shouldReturnFalseForInvalidId() {
        assertFalse(orderRepository.existsById(99999));
    }
//    @Test
//    void shouldReturnEmptyList() {
//        orderRepository.deleteAll();
//        assertTrue(orderRepository.findAll().isEmpty());
//    }


}
