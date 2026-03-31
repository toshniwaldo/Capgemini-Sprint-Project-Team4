package com.example.DemoCheck.projection;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import com.example.DemoCheck.entity.OrderDetails;

@Projection(name = "orderDetailView", types = { OrderDetails.class })
public interface OrderDetailProjection {

    Integer getQuantityOrdered();
    BigDecimal getPriceEach();

    @Value("#{target.product.productName}")
    String getProductName();

    @Value("#{target.order.orderNumber}")
    Integer getOrderNumber();

    @Value("#{target.priceEach * target.quantityOrdered}")
    BigDecimal getTotalPrice();
}