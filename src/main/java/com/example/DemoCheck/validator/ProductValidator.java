package com.example.DemoCheck.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.example.DemoCheck.entity.Product;
import com.example.DemoCheck.repository.ProductRepository;

@Component
public class ProductValidator implements Validator {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return Product.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Product p = (Product) target;

        // productName
        if (p.getProductName() == null || p.getProductName().trim().isEmpty()) {
            errors.rejectValue("productName", "productName.empty", "Product name is required");
        }

        // productVendor
        if (p.getProductVendor() == null || p.getProductVendor().trim().isEmpty()) {
            errors.rejectValue("productVendor", "productVendor.empty", "Vendor is required");
        }

        // productScale
        if (p.getProductScale() == null || p.getProductScale().trim().isEmpty()) {
            errors.rejectValue("productScale", "productScale.empty", "Scale is required");
        }

        // productDescription
        if (p.getProductDescription() == null || p.getProductDescription().trim().isEmpty()) {
            errors.rejectValue("productDescription", "productDescription.empty", "Description is required");
        }

        // quantity
        if (p.getQuantityInStock() == null || p.getQuantityInStock() < 0) {
            errors.rejectValue("quantityInStock", "quantity.invalid", "Quantity must be >= 0");
        }

        // prices
        if (p.getBuyPrice() < 0) {
            errors.rejectValue("buyPrice", "buyPrice.invalid", "Buy price must be >= 0");
        }

        if (p.getMSRP() < 0) {
            errors.rejectValue("MSRP", "MSRP.invalid", "MSRP must be >= 0");
        }
    }
}
