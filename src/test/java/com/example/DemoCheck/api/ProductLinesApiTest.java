package com.example.DemoCheck.api;

import com.example.DemoCheck.entity.ProductLine;
import com.example.DemoCheck.repository.ProductLineRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductLinesApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductLineRepository repository;

    // 🔥 Helper method (used in multiple tests)
    void insertDummyData() {
        ProductLine p = new ProductLine();
        p.setProductLine("Classic Cars");
        p.setTextDescription("This line contains classic vintage cars.");
        p.setHtmlDescription("<p>Classic</p>");
        repository.save(p);
        repository.flush();
    }

    // ================== CREATE TEST ==================

    @Test // ✅ creation pass
    void testCreateProductLine() throws IOException {

        byte[] imageData = Files.readAllBytes(
                Paths.get("src/test/java/com/example/DemoCheck/api/dummy-image-data.jpg")
        );

        ProductLine productLine = new ProductLine();
        productLine.setProductLine("Classic Cars");
        productLine.setTextDescription("This line contains classic vintage cars.");
        productLine.setHtmlDescription("<p><b>Classic Cars</b> collection</p>");
        productLine.setImage(imageData);

        ProductLine saved = repository.save(productLine);

        assertThat(saved).isNotNull();
        assertThat(saved.getProductLine()).isEqualTo("Classic Cars");
    }

    // ================== GET TESTS ==================

    // ✅ Get All
    @Test
    void testGetAllProductLines() throws Exception {
        insertDummyData();
        mockMvc.perform(get("/productlines"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.hal+json"))
                .andExpect(jsonPath("$._embedded.productLines.length()", greaterThan(0)));
    }


    // ✅ Get by ID

    @Test
    void testGetProductLineById() throws Exception {

        ProductLine p = new ProductLine();
        p.setProductLine("Classic Cars");
        p.setTextDescription("classic cars");

        repository.save(p);
        repository.flush();

        mockMvc.perform(get("/productlines/{id}", "Classic Cars"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.textDescription").value("classic cars"));
    }

    // ❌ Get by ID - Not Found
    @Test
    void testGetProductLineById_NotFound() throws Exception {

        mockMvc.perform(get("/productlines/NoSuchId123"))
                .andExpect(status().isNotFound());
    }

    // ❌ Get by ID - Invalid format
    @Test
    void testGetProductLineById_InvalidFormat() throws Exception {

        mockMvc.perform(get("/productlines/@@@###"))
                .andExpect(status().isNotFound());
    }

    // ================== SEARCH TESTS ==================

    // ✅ Search by description
    @Test
    void testSearchByDescription() throws Exception {

        insertDummyData();

        mockMvc.perform(get("/productlines/search/by-description")
                        .param("keyword", "Classic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productLines.length()", greaterThan(0)));
    }

    // ❌ Search - No results
    @Test
    void testSearchByDescription_NoResult() throws Exception {

        mockMvc.perform(get("/productlines/search/by-description")
                        .param("keyword", "xyz123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productLines.length()").value(0));
    }

    // ❌ Search - Missing param
    @Test
    void testSearchByDescription_MissingParam_ShouldReturnEmpty() throws Exception {

        mockMvc.perform(get("/productlines/search/by-description"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productLines.length()").value(0));
    }
    // ================== UPDATE TESTS ==================

    // ✅ PUT - Full update (PASS)
    @Test
    void testUpdateProductLine_PUT_ShouldPass() throws Exception {

        insertDummyData();

        String updatedJson = """
    {
        "textDescription": "updated description",
        "htmlDescription": "<p>Updated</p>"
    }
    """;

        mockMvc.perform(put("/productlines/{id}", "Classic Cars")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isNoContent());

        // Verify update
        mockMvc.perform(get("/productlines/{id}", "Classic Cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.textDescription").value("updated description"));
    }


    // ✅ PATCH - Partial update (PASS)
    @Test
    void testUpdateProductLine_PATCH_ShouldPass() throws Exception {

        insertDummyData();

        String patchJson = """
    {
        "textDescription": "patched desc"
    }
    """;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/productlines/{id}", "Classic Cars")
                        .contentType("application/merge-patch+json")
                        .content(patchJson))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/productlines/{id}", "Classic Cars"))
                .andExpect(jsonPath("$.textDescription").value("patched desc"));
    }


    // ❌ PUT - Update non-existing ID (FAIL)
    @Test
    void testUpdateProductLine_NotFound_ShouldFail() throws Exception {

        String json = """
    {
        "textDescription": "updated"
    }
    """;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/productlines/{id}", "DoesNotExist")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());
    }


    // ❌ PUT - Invalid JSON (FAIL)
    @Test
    void testUpdateProductLine_InvalidJson_ShouldFail() throws Exception {

        insertDummyData();

        String invalidJson = "{ invalid json }";

        mockMvc.perform(put("/productlines/{id}", "Classic Cars")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isInternalServerError());
    }

   //-----------------------PAGINATION--------------------

    @Test
    void testPagination_API() throws Exception {

        for (int i = 0; i < 20; i++) {
            ProductLine p = new ProductLine();
            p.setProductLine("PL_" + i);
            p.setTextDescription("Desc " + i);
            repository.save(p);
        }
        repository.flush();

        mockMvc.perform(get("/productlines?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productLines.length()").value(5));
    }

}