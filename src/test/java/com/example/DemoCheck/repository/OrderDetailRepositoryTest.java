package com.example.DemoCheck.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Transactional;

import com.example.DemoCheck.entity.Customer;
import com.example.DemoCheck.entity.Order;
import com.example.DemoCheck.entity.OrderDetailId;
import com.example.DemoCheck.entity.OrderDetails;
import com.example.DemoCheck.entity.Product;
import com.example.DemoCheck.entity.ProductLine;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class OrderDetailRepositoryTest {
    @Autowired 
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductLineRepository productLineRepository;
    
    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setup() {

        ProductLine line = new ProductLine();
        line.setProductLine("Guns");
        line.setTextDescription("Gun variants");
        productLineRepository.save(line);

        Product product = new Product();
        product.setProductCode("S10_89876");
        product.setProductName("AK-47");
        product.setProductLine(line);
        product.setProductVendor("Kalashnikov Concern");
        product.setProductScale("1:10");
        product.setProductDescription("Kalashnikov AK-47 Rifle");
        product.setQuantityInStock(1000);
        product.setBuyPrice(6000);
        product.setMSRP(6500);
        productRepository.save(product);

        Customer customer = new Customer();
        customer.setCustomerNumber(550);
        customer.setCustomerName("Test Customer");

        // set required fields based on your schema
        customer.setContactLastName("Doe");
        customer.setContactFirstName("John");
        customer.setPhone("1234567890");
        customer.setAddressLine1("Test Address");
        customer.setCity("Test City");
        customer.setCountry("India");

        customerRepository.save(customer);

        Order order = new Order();
        order.setOrderNumber(10500);
        order.setOrderDate(LocalDate.now());
        order.setRequiredDate(LocalDate.now().plusDays(5));
        order.setStatus("Shipped");
        order.setCustomer(customer);
        orderRepository.save(order);

        OrderDetailId id = new OrderDetailId();
        id.setOrderNumber(10500);
        id.setProductCode("S10_89876");

        OrderDetails orderDetail = new OrderDetails();
        orderDetail.setId(id);
        orderDetail.setProduct(product);
        orderDetail.setOrder(order);
        orderDetail.setQuantityOrdered(50);
        orderDetail.setPriceEach(new BigDecimal("6500"));
        orderDetail.setOrderLineNumber(new Short("1"));

        orderDetailRepository.save(orderDetail);
    }

    @Test
    void testFindByProduct_ProductCode() {
        List<OrderDetails> result = orderDetailRepository.findByProduct_ProductCode("S10_89876");

        assertNotNull(result);
        assertEquals(1, result.size());

        OrderDetails orderDetailRecord = result.get(0);
        assertEquals("S10_89876", orderDetailRecord.getProduct().getProductCode());
        assertEquals(50, orderDetailRecord.getQuantityOrdered());
    }
}
