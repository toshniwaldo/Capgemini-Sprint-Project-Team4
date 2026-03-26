package com.example.DemoCheck.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "orderdetails")
public class OrderDetails {
    @EmbeddedId
    private OrderDetailId id;

    @ManyToOne
    @MapsId("orderNumber")
    @JoinColumn(name = "orderNumber", nullable = false)
    private Order order;

    @ManyToOne
    @MapsId("productCode")
    @JoinColumn(name = "productCode", nullable = false)
    private Product product;

    @Column(name = "quantityOrdered")
    private Integer quantityOrdered;

    @Column(name = "priceEach")
    private BigDecimal priceEach;

    @Column(name = "orderLineNumber")
    private Short orderLineNumber;
}
