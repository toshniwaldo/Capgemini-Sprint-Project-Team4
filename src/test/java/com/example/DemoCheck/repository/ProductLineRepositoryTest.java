package com.example.DemoCheck.repository;

import com.example.DemoCheck.entity.ProductLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductLineRepositoryTest {

    @Autowired
    private ProductLineRepository repository;

    @Test
    @DisplayName("Search productLine by name - exists (PASS)")
    void testSearchProductLineExists() {
        ProductLine pl = new ProductLine();
        pl.setProductLine("Classic Cars");
        pl.setTextDescription("desc");
        repository.save(pl);

        Optional<ProductLine> result = repository.findById("Classic Cars");
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Search productLine by name - not exists (FAIL)")
    void testSearchProductLineNotExists() {
        Optional<ProductLine> result = repository.findById("Bikes");
        assertThat(result).isNotPresent();
    }

}