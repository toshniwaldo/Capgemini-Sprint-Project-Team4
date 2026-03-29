package com.example.DemoCheck.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


import org.springframework.transaction.annotation.Transactional;

import com.example.DemoCheck.entity.Product;
import com.example.DemoCheck.entity.ProductLine;
import com.example.DemoCheck.repository.ProductLineRepository;
import com.example.DemoCheck.repository.ProductRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductLineRepository productLineRepository;

    @BeforeEach
    void setup() {
        ProductLine pl = new ProductLine();
        pl.setProductLine("Jets");
        pl.setTextDescription("Jet models");
        if (!productLineRepository.existsById("Jets")) {
            productLineRepository.save(pl);
        }

        Product p = new Product();
        p.setProductCode("S10_9999");
        p.setProductName("Existing");
        p.setProductLine(pl);
        p.setProductVendor("Vendor");
        p.setProductScale("1:18");
        p.setProductDescription("Test description");
        p.setQuantityInStock(10);
        p.setBuyPrice(100.0);
        p.setMSRP(150.0); 

        productRepository.save(p);
    }

    // ================================================ GET Products API ================================================

    // valid request test
    @Test
    void testGetProducts_byPage_returnsPage() throws Exception {
        mockMvc.perform(get("/products")
                    .param("page", "0")
                    .param("size", "20")
                    .param("projection", "productView")
                    .param("sort", "productName,asc")
                )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.size").value(20))
            .andExpect(jsonPath("$.page.number").value(0))
            .andExpect(jsonPath("$._embedded.products").exists());
    }

    // ------------------ pagination tests starts here ------------------

    // testing first page fetch
    @Test
    void testFirstPage() throws Exception {
       mockMvc.perform(get("/products")
        .param("page", "0")
        .param("size", "20")
        .param("projection", "productView"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.number").value(0));
    }

    // testing middle page fetch
    @Test
    void testMiddlePage() throws Exception {
        mockMvc.perform(get("/products?page=3&size=20")
                .param("projection", "productView"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.number").value(3));
    }

    // testing last page fetch
    @Test
    void testLastPage() throws Exception {
        mockMvc.perform(get("/products?page=5&size=20")
                .param("projection", "productView"))
            .andExpect(status().isOk());
    }

    // testing page out of bounds
    // should return empty products list
    @Test
    void testOutOfBoundsPage() throws Exception {
        mockMvc.perform(get("/products?page=555&size=20")
                .param("projection", "productView"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.products").isEmpty());
    }

    // negative page number 
    @Test
    void testNegativePage() throws Exception {
        mockMvc.perform(get("/products?page=-1&size=20")
                .param("projection", "productView"))
            // .andExpect(status().isBadRequest()); 
            /*
            when we give negative page number as param, then spring internally converts 
            that negative page number to 0
            -1, -3, -78 -> 0

            Hence, when we use isBadRequest(), test fails, because spring is returning 
            status = 200 here

            hence we should use isOk() and validate page number as 0
            */


            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.number").value(0));
    }

    // size = 0 edge case
    @Test
    void testSizeZero() throws Exception {
        mockMvc.perform(get("/products?page=0&size=0")
                .param("projection", "productView"))
            .andExpect(status().isOk());
    }

    // ------------------ sorting tests starts here ------------------

    // sorting on a field name
    @Test
    void testAscendingProductSorting() throws Exception {
        String response = mockMvc.perform(get("/products?sort=productName,asc")
                .param("projection", "productView"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.products").isArray())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        JsonNode products = root.path("_embedded").path("products");

        if (products.size() < 2) return;

        String first = products.get(0).path("productName").asString();
        String second = products.get(1).path("productName").asString();    

        assertTrue(first.compareToIgnoreCase(second) <= 0);
    }

    // reverse sorting on a field name
    @Test
    void testDescendingProductSorting() throws Exception {
        String response = mockMvc.perform(get("/products?sort=productName,desc")
                .param("projection", "productView"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.products").isArray())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        JsonNode products = root.path("_embedded").path("products");

        if (products.size() < 2) return;

        String first = products.get(0).path("productName").asString();
        String second = products.get(1).path("productName").asString();    

        assertTrue(first.compareToIgnoreCase(second) >= 0);
    }

    // invalid sort field provided
    @Test
    void testInvalidSortField() throws Exception {
        mockMvc.perform(get("/products?sort=invalidField,asc")
                .param("projection", "productView"))
            // .andExpect(status().isBadRequest());

            /*
            ideally sending invalid field should throw bad request 400, but spring internally handles it
            it ignores the incorrect value and fallback to unsorted result
            */
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.products").exists());
    }

    // ================================================ GET Products Search API ================================================

    // search with a valid keyboard
    @Test
    void testSearch_validKeyword_result() throws Exception {
        mockMvc.perform(get("/products/search/searchByNameOrLine")
                .param("name", "Existing")
                .param("line", "jet")
                .param("page", "0")
                .param("size", "20")
                .param("projection", "productView"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.products").isNotEmpty());
    }

    // search with a no match keyword
    @Test
    void testSearch_noMatch() throws Exception {
        mockMvc.perform(get("/products/search/searchByNameOrLine")
                .param("line", "metro")
                .param("page", "0")
                .param("size", "20")
                .param("projection", "productView"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.products").isEmpty());
    }

    // search with a partial match
    @Test
    void testSearch_PartialMatch() throws Exception {
        mockMvc.perform(get("/products/search/searchByNameOrLine")
                .param("name", "xis")
                .param("line", "je")
                .param("projection", "productView"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.products").isNotEmpty());
    }


    // ================================================ GET Products By Id API ================================================

    // valid ID
    @Test
    void testGetProductByValidId() throws Exception {
        mockMvc.perform(get("/products/S10_9999")
                .param("projection", "productView"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productName").exists());
    }

    // invalid id
    @Test
    void testGetProductByInvalidId() throws Exception {
        mockMvc.perform(get("/products/S10_1678025")
                .param("projection", "productView"))
            .andExpect(status().isNotFound());
    }


    // ================================================ Projection Tests ================================================

    // checks valid projection
    @Test
    void testProjection_ProductView() throws Exception {
        mockMvc.perform(get("/products")
                .param("projection", "productView"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.products[0].productName").exists())
            .andExpect(jsonPath("$._embedded.products[0].buyPrice").exists());
    }

    // checking non-member field of projection in response
    @Test
    void testProjection_NoHiddenFieldResponse() throws Exception {
        mockMvc.perform(get("/products")
                .param("projection", "productView"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.products[0].productCode").doesNotExist());
    }




    // ================================================ POST Product tests ================================================
    
    // tests Product creation through POST endpoint
    @Test
    void testCraeteProduct_Valid() throws Exception {
        String json = """
            {
                "productCode": "S10_8888",
                "productName": "Test Product",
                "productLine": "/productLines/Jets",
                "productVendor": "Test Vendor",
                "productScale": "1:18",
                "productDescription": "Test description",
                "quantityInStock": 100,
                "buyPrice": 200.0,
                "MSRP": 300.0
            }
            """;

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"));
    }

    // tests Product creation failure due to missing fields in JSON through POST endpoint
    @Test
    void testCreateProduct_MissingField() throws Exception {
        String json = """
            {
                "productCode": "S10_1111",
                "productLine": "/productLines/Jets",
                "productVendor": "Vendor",
                "productScale": "1:18",
                "productDescription": "Desc",
                "quantityInStock": 10,
                "buyPrice": 100.0,
                "MSRP": 150.0
            }
        """;

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    // tests Product creation failure due to blank fields in JSON through POST endpoint
    @Test
    void testCreateProduct_BlankProductName() throws Exception {
        String json = """
            {
                "productCode": "S10_2222",
                "productName": "   ",
                "productLine": "/productLines/Jets",
                "productVendor": "Vendor",
                "productScale": "1:18",
                "productDescription": "Desc",
                "quantityInStock": 10,
                "buyPrice": 100.0,
                "MSRP": 150.0
            }
        """;

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    // tests Product creation failure due to missing relationship mapping field in JSON through POST endpoint
    @Test
    void testCreateProduct_MissingProductLine() throws Exception {
        String json = """
            {
                "productCode": "S10_3333",
                "productName": "Test",
                "productVendor": "Vendor",
                "productScale": "1:18",
                "productDescription": "Desc",
                "quantityInStock": 10,
                "buyPrice": 100.0,
                "MSRP": 150.0
            }
        """;

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    // tests Product creation failure due to non-existing relationship mapping field in JSON through POST endpoint
    @Test
    void testCreateProduct_InvalidProductLine() throws Exception {
        String json = """
            {
                "productCode": "S10_4444",
                "productName": "Test",
                "productLine": "/productLines/InvalidLine",
                "productVendor": "Vendor",
                "productScale": "1:18",
                "productDescription": "Desc",
                "quantityInStock": 10,
                "buyPrice": 100.0,
                "MSRP": 150.0
            }
        """;

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    // tests Product creation failure due to negative value in numeric field in JSON through POST endpoint
    @Test
    void testCreateProduct_NegativeQuantity() throws Exception {
        String json = """
            {
                "productCode": "S10_5555",
                "productName": "Test",
                "productLine": "/productLines/Jets",
                "productVendor": "Vendor",
                "productScale": "1:18",
                "productDescription": "Desc",
                "quantityInStock": -5,
                "buyPrice": 100.0,
                "MSRP": 150.0
            }
        """;

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    // tests Product creation failure due to negative value in numeric field in JSON through POST endpoint
    @Test
    void testCreateProduct_NegativePrice() throws Exception {
        String json = """
            {
                "productCode": "S10_6666",
                "productName": "Test",
                "productLine": "/productLines/Jets",
                "productVendor": "Vendor",
                "productScale": "1:18",
                "productDescription": "Desc",
                "quantityInStock": 10,
                "buyPrice": -100.0,
                "MSRP": 150.0
            }
        """;

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    // tests Product creation failure due to duplicate product insertion in JSON through POST endpoint
    @Test
    void testCreateProduct_DuplicateProductCode() throws Exception {
        String json = """
            {
                "productCode": "S10_9999",
                "productName": "Duplicate",
                "productLine": "/productLines/Jets",
                "productVendor": "Vendor",
                "productScale": "1:18",
                "productDescription": "Desc",
                "quantityInStock": 10,
                "buyPrice": 100.0,
                "MSRP": 150.0
            }
        """;

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest()); // or isBadRequest depending on handler
    }

    
    // ================================================ PUT/PATCH Product tests ================================================

    // tests Product updation through PUT endpoint
    @Test
    void testUpdateProduct_ValidPut() throws Exception {
        String json = """
        {
            "productName": "Updated Name",
            "productLine": "/productLines/Jets",
            "productVendor": "Updated Vendor",
            "productScale": "1:24",
            "productDescription": "Updated Desc",
            "quantityInStock": 50,
            "buyPrice": 200.0,
            "MSRP": 300.0
        }
        """;

        mockMvc.perform(put("/products/S10_9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isNoContent()); // 204
    }
}
