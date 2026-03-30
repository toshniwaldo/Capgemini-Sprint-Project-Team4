package com.example.DemoCheck.projection;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;
import com.example.DemoCheck.entity.Product;


// custom view for Product table, hides table id
@Projection(name = "productView", types = { Product.class })
public interface ProductProjection {
    String getProductName();
    @Value("#{target.productLine.productLine}")
    String getProductLine();
    String getProductVendor();
    Integer getQuantityInStock();
    BigDecimal getBuyPrice();
    BigDecimal getMSRP();
}
