package com.example.DemoCheck.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

        Page<OrderDetails> page = orderDetailRepository
                .findByProduct_ProductCode("S10_89876", PageRequest.of(0, 10));

        List<OrderDetails> result = page.getContent();

        assertNotNull(result);
        assertEquals(1, result.size());

        OrderDetails orderDetailRecord = result.get(0);
        assertEquals("S10_89876", orderDetailRecord.getProduct().getProductCode());
        assertEquals(50, orderDetailRecord.getQuantityOrdered());
    }

    @Test
    void testFindByProduct_ProductCode_NoRecords() {

        Page<OrderDetails> page = orderDetailRepository
                .findByProduct_ProductCode("S18_3233", PageRequest.of(0, 10));

        assertNotNull(page);
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void testFindByProduct_InvalidProductCode() {

        Page<OrderDetails> page = orderDetailRepository
                .findByProduct_ProductCode("INVALID_CODE", PageRequest.of(0, 10));

        assertNotNull(page);
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void testFindByProduct_ProductCode_MultipleRecords() {

        Page<OrderDetails> page = orderDetailRepository
                .findByProduct_ProductCode("S10_89876", PageRequest.of(0, 10));

        assertNotNull(page);
        assertTrue(page.getContent().size() >= 1);
    }

    // null input test
    @Test
    void testFindByProduct_ProductCode_nullInput() {

        Page<OrderDetails> page =
                orderDetailRepository.findByProduct_ProductCode(null, PageRequest.of(0, 10));

        assertTrue(page.getContent().isEmpty());
    }

    // relationship integrity
    @Test
    void testOrderDetailDataIntegrity() {

        Page<OrderDetails> page =
                orderDetailRepository.findByProduct_ProductCode("S10_89876", PageRequest.of(0, 10));

        OrderDetails od = page.getContent().get(0);

        assertNotNull(od.getOrder());
        assertNotNull(od.getProduct());
    }

    // derived field testing
    @Test
    void testTotalPriceCalculation() {
        OrderDetailId id = new OrderDetailId();
        id.setOrderNumber(10500);
        id.setProductCode("S10_89876");

        OrderDetails orderDetail = orderDetailRepository.findById(id).get();

        BigDecimal expected = new BigDecimal("325000");

        BigDecimal actual = orderDetail.getPriceEach().multiply(BigDecimal.valueOf(orderDetail.getQuantityOrdered()));

        assertEquals(expected, actual);
    }
}
