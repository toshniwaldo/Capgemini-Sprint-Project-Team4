package com.example.DemoCheck.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class OrderDetailId implements Serializable {
    private Integer orderNumber;
    private String productCode;
}
