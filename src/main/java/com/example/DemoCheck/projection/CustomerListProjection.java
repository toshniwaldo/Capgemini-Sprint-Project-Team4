package com.example.DemoCheck.projection;

import com.example.DemoCheck.entity.Customer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.math.BigDecimal;

@Projection(name = "CustomerListProjection", types = Customer.class)
public interface CustomerListProjection {

    @Value("#{target.customerName}")
    String getCustomerName();

    @Value("#{target.contactFirstName + ' ' + target.contactLastName}")
    String getContactName();

    @Value("#{target.phone}")
    String getPhoneNumber();

    @Value("#{target.city + ', ' + target.country}")
    String getLocation();

    BigDecimal getCreditLimit();
}
