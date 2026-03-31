package com.example.DemoCheck.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerPatchDTO {

    private String customerName;
    private String contactFirstName;
    private String contactLastName;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postalCode;
    private String country;
    private BigDecimal creditLimit;
    private Integer salesRepEmployeeNumber;
}
