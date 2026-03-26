package com.example.DemoCheck.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "products")
public class Product {
    @Id
    @Column(name = "productCode")
    private String productCode;

    @Column(name = "productName")
    private String productName;

    @Column(name = "productLine")
    private String productLine;

    @Column(name = "productVendor")
    private String productVendor;

    @Column(name = "productScale")
    private String productScale;

    @Column(name = "productDescription")
    private String productDescription;

    @Column(name = "quantityInStock")
    private int quantityInStock;

    @Column(name = "buyPrice")
    private double buyPrice;

    @Column(name = "MSRP")
    private double MSRP;

    @OneToMany(mappedBy = "product")
    private List<OrderDetails> orderDetails;
}
