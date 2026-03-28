package com.example.DemoCheck.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import com.example.DemoCheck.entity.Product;
import com.example.DemoCheck.repository.ProductLineRepository;
import com.example.DemoCheck.repository.ProductRepository;

@RepositoryEventHandler(Product.class)
@Component
public class ProductEventHandler {

    @Autowired
    private ProductLineRepository productLineRepository;
    @Autowired
    private ProductRepository productRepository;

    @HandleBeforeCreate
    public void handleBeforeCreate(Product p) {
        if (productRepository.existsById(p.getProductCode())) {
            throw new IllegalArgumentException("Product already exists");
        }

        validateProductLine(p);
    }

    @HandleBeforeSave
    public void handleBeforeSave(Product p) {
        validateProductLine(p);
    }

    private void validateProductLine(Product p) {
        if (p.getProductLine() == null) {
            throw new IllegalArgumentException("Product line is required");
        }

        if (p.getProductLine().getProductLine() == null) {
            throw new IllegalArgumentException("Product line is invalid");
        }

        if (!productLineRepository.existsById(p.getProductLine().getProductLine())) {

            throw new RuntimeException("Invalid or missing product line");
        }
    }
}