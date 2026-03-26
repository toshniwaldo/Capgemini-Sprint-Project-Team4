package com.example.DemoCheck.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Transactional;

import com.example.DemoCheck.entity.Product;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @BeforeEach
    void setUp() {
        Product p1 = new Product();

        p1.setProductCode("S10_89876");
        p1.setProductName("Parker pen");
        p1.setProductLine("Planes");
        p1.setProductVendor("Novelty");
        p1.setProductScale("1:10");
        p1.setProductDescription("Parker pen");
        p1.setQuantityInStock(10);
        p1.setBuyPrice(60);
        p1.setMSRP(65);

        Product p2 = new Product();

        p2.setProductCode("S10_89856");
        p2.setProductName("Trimax");
        p2.setProductLine("Planes");
        p2.setProductVendor("Novelty");
        p2.setProductScale("1:10");
        p2.setProductDescription("Trimax pen");
        p2.setQuantityInStock(10);
        p2.setBuyPrice(60);
        p2.setMSRP(65);

        productRepository.save(p1);
        productRepository.save(p2);
    }

    @Test
    void testSavedProduct() {
        Product saved = productRepository.findById("S10_89876").get();

        assertNotNull(saved);
        assertEquals("S10_89876", saved.getProductCode());
        assertEquals("Parker pen", saved.getProductName());
        assertEquals("Planes", saved.getProductLine());
        assertEquals("Novelty", saved.getProductVendor());
        assertEquals(10, saved.getQuantityInStock());
        assertEquals(60, saved.getBuyPrice());
        assertEquals(65, saved.getMSRP());
    }

    @Test
    void testFindAllProducts() {
        List<Product> productList = productRepository.findAll();

        // general tests
        assertNotNull(productList);
        assertTrue(productList.size() >= 2);

        // specific tests
        assertTrue(productList.stream().anyMatch(p -> p.getProductCode().equals("S10_89876")));
        assertTrue(productList.stream().anyMatch(p -> p.getProductCode().equals("S10_89856")));
    }

    @Test
    void testFindAllProducts_NoProducts() {
        orderDetailRepository.deleteAll();
        productRepository.deleteAll();

        List<Product> productList = productRepository.findAll();

        assertNotNull(productList);
        assertTrue(productList.isEmpty());
    }
}
