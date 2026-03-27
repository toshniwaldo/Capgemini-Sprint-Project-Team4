package com.example.DemoCheck.projection;

import com.example.DemoCheck.entity.Office;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "officeList",types = Office.class)
public interface OfficeProjection {

    String getOfficeCode();
    String getCity();
    String getCountry();
    String getPhone();
    String getTerritory();
}
