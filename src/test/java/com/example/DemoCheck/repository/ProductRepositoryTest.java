package com.example.DemoCheck.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import com.example.DemoCheck.entity.Product;
import com.example.DemoCheck.entity.ProductLine;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductLineRepository productLineRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    // setup for tests
    @BeforeEach
    void setUp() {
        ProductLine line = new ProductLine();
        line.setProductLine("Planes");
        line.setTextDescription("Plane models");
        productLineRepository.save(line);

        Product p1 = new Product();

        p1.setProductCode("S10_89876");
        p1.setProductName("Parker pen");
        p1.setProductLine(line);
        p1.setProductVendor("Novelty");
        p1.setProductScale("1:10");
        p1.setProductDescription("Parker pen");
        p1.setQuantityInStock(10);
        p1.setBuyPrice(60);
        p1.setMSRP(65);

        Product p2 = new Product();

        p2.setProductCode("S10_89856");
        p2.setProductName("Trimax");
        p2.setProductLine(line);
        p2.setProductVendor("Novelty");
        p2.setProductScale("1:10");
        p2.setProductDescription("Trimax pen");
        p2.setQuantityInStock(10);
        p2.setBuyPrice(60);
        p2.setMSRP(65);

        productRepository.save(p1);
        productRepository.save(p2);
    }

    // product object creation test
    @Test
    void testSavedProduct() {
        Product saved = productRepository.findById("S10_89876").get();

        assertNotNull(saved);
        assertEquals("S10_89876", saved.getProductCode());
        assertEquals("Parker pen", saved.getProductName());
        assertEquals("Planes", saved.getProductLine().getProductLine());
        assertEquals("Novelty", saved.getProductVendor());
        assertEquals(10, saved.getQuantityInStock());
        assertEquals(60, saved.getBuyPrice());
        assertEquals(65, saved.getMSRP());
    }

    // this tests findAll functionality
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

    // this tests findALl functionality in case of empty DB
    @Test
    void testFindAllProducts_NoProducts() {
        orderDetailRepository.deleteAll();
        productRepository.deleteAll();

        List<Product> productList = productRepository.findAll();

        assertNotNull(productList);
        assertTrue(productList.isEmpty());
    }

    // this tests findBy valid Id 
    @Test
    void testFindById() {
        Optional<Product> result = productRepository.findById("S10_89856");

        assertTrue(result.isPresent());
        assertEquals("Trimax", result.get().getProductName());
    }

    // this tests find by invalid Id
    @Test
    void testFindByInvalidId_NoProduct() {
        Optional<Product> result = productRepository.findById("S10_89222");

        assertFalse(result.isPresent());
    }

    // pagination testing -> tests and verifies the pagination functionality
    @Test
    void testFindAll_Pagination() {
        for (int i = 0; i < 20 ; i++) {
            Product p = new Product();
            p.setProductCode("S10_" + i);
            p.setProductName("Product " + i);
            p.setProductLine(productLineRepository.findById("Planes").get());
            p.setProductVendor("Vendor");
            p.setProductScale("1:18");
            p.setProductDescription("Desc");
            p.setQuantityInStock(10);
            p.setBuyPrice(100.0);
            p.setMSRP(150.0);
            productRepository.save(p);
        } // creating records in DB

        Page<Product> pages = productRepository.findAll(PageRequest.of(0, 20));

        int totalRecords = (int) pages.getTotalElements();
        int expectedPages = (int) Math.ceil((double)totalRecords/20);

        assertEquals(20, pages.getContent().size());
        assertEquals(expectedPages, pages.getTotalPages()); // 25 / 20 = 2
    }

    // custom search query testing with valid name or line
    @Test
    void testSearchByNameOrLine() {
        Page<Product> result = productRepository.findByProductNameContainingIgnoreCaseOrProductLine_ProductLineContainingIgnoreCase(null, "plane", PageRequest.of(0,20));

        assertFalse(result.isEmpty());
    }

    // custom search query testing with invalid name or line
    @Test
    void testSearchByNameOrLine_NoResults() {
        Page<Product> result = productRepository.findByProductNameContainingIgnoreCaseOrProductLine_ProductLineContainingIgnoreCase("H&M", "jacket", PageRequest.of(0,20));

        assertTrue(result.isEmpty());
    }

    // custom search query testing with valid name or line ignoring case sensitivity
    @Test
    void testSearchCaseInsensitivity() {
        Page<Product> result = productRepository.findByProductNameContainingIgnoreCaseOrProductLine_ProductLineContainingIgnoreCase(
            null, "plANES", PageRequest.of(0, 10)
        );

        assertFalse(result.isEmpty());
    }

    // this tests the update functionality
    @Test
    void testUpdateProduct() {
        Product existing = productRepository.findById("S10_89876").get();
        existing.setProductName("Updated Name");

        Product updated = productRepository.save(existing);

        assertEquals("Updated Name", updated.getProductName());
    }


    // updating non-existing entity, JPA internally saves it as new entity
    @Test
    void testUpdate_NonExistingProduct_ShouldCreate() {
        Product p = new Product();
        p.setProductCode("NEW_123");
        p.setProductName("New Product");
        p.setProductLine(productLineRepository.findById("Planes").get());

        Product saved = productRepository.save(p);

        assertNotNull(saved);
        assertEquals("NEW_123", saved.getProductCode());
    }

    // constraint validation -> invalid value, violating DB and JPA constraints
    @Test
    void testSave_InvalidProduct_ShouldFail() {
        Product p = new Product();
        p.setProductCode(null); // invalid

        assertThrows(Exception.class, () -> productRepository.save(p));
    }

    // constraint validation -> updating a record with an invalid value for a variable
    @Test
    void testUpdate_InvalidData_ShouldFail() {
        Product existing = productRepository.findById("S10_89876").get();

        existing.setProductName(null); // assuming NOT NULL

        assertThrows(Exception.class, () -> {
            productRepository.save(existing);
            productRepository.flush();
        });
    }

}
