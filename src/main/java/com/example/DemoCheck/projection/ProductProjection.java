package com.example.DemoCheck.projection;

import java.math.BigDecimal;

import org.springframework.data.rest.core.config.Projection;

import com.example.DemoCheck.entity.Product;
import com.example.DemoCheck.entity.ProductLine;

// custom view for Product table, hides table id
@Projection(name = "productView", types = Product.class)
public interface ProductProjection {
    String getProductName();
    ProductLineView getProductLine();
    String getProductVendor();
    Integer getQuantityInStock();
    BigDecimal getBuyPrice();
    BigDecimal getMSRP();
}
