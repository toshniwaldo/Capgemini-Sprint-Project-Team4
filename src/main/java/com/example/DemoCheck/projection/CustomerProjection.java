package com.example.DemoCheck.projection;

import com.example.DemoCheck.entity.Customer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "customerView", types = Customer.class)
public interface CustomerProjection {
    Integer getCustomerNumber();
    String getCustomerName();

    @Value("#{target.contactFirstName + ' ' + target.contactLastName}")
    String getContactName();

    String getPhone();

    @Value("#{target.addressLine1 + (target.addressLine2 != null ? ', ' + target.addressLine2 : '')}")
    String getAddress();

    String getCity();

    String getCountry();

    java.math.BigDecimal getCreditLimit();
}