package com.example.DemoCheck.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
@Table(name = "products")
public class Product {
    @Id
    @Column(name = "productCode")
    private String productCode;

    @NotBlank
    @Column(name = "productName", nullable = false)
    private String productName;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "productLine", referencedColumnName = "productLine")
    private ProductLine productLine;

    @NotBlank
    @Column(name = "productVendor", nullable = false)
    private String productVendor;

    @NotBlank
    @Column(name = "productScale", nullable = false)
    private String productScale;

    @NotBlank
    @Column(name = "productDescription", nullable = false)
    private String productDescription;

    @NotNull
    @Min(0)
    @Column(name = "quantityInStock", nullable = false)
    private Integer quantityInStock;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "buyPrice", nullable = false)
    private double buyPrice;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "MSRP", nullable = false)
    private double MSRP;

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<OrderDetails> orderDetails;
}
