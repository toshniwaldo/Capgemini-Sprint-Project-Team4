package com.example.DemoCheck.handler;

import com.example.DemoCheck.entity.Office;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@RepositoryEventHandler(Office.class)
@Component
public class OfficeEventHandler {

    // POST
    @HandleBeforeCreate
    public void validateBeforeCreate(Office office) {
        validateOffice(office);
    }

    // PUT + PATCH
    @HandleBeforeSave
    public void validateBeforeSave(Office office) {
        validateOffice(office);
    }

    private void validateOffice(Office office) {

        // Office Code (important for POST)
        if (!StringUtils.hasText(office.getOfficeCode())) {
            throw new IllegalArgumentException("Office Code cannot be blank");
        }

        if (office.getOfficeCode().length() > 10) {
            throw new IllegalArgumentException("Office Code max length is 10");
        }

        // City
        if (!StringUtils.hasText(office.getCity())) {
            throw new IllegalArgumentException("City cannot be blank");
        }

        if (office.getCity().length() > 50) {
            throw new IllegalArgumentException("City max length is 50");
        }

        // Phone
        if (!StringUtils.hasText(office.getPhone())) {
            throw new IllegalArgumentException("Phone cannot be blank");
        }

        if (office.getPhone().length() > 50) {
            throw new IllegalArgumentException("Phone max length is 50");
        }

        // AddressLine1
        if (!StringUtils.hasText(office.getAddressLine1())) {
            throw new IllegalArgumentException("AddressLine1 cannot be blank");
        }

        if (office.getAddressLine1().length() > 50) {
            throw new IllegalArgumentException("AddressLine1 max length is 50");
        }

        // Country
        if (!StringUtils.hasText(office.getCountry())) {
            throw new IllegalArgumentException("Country cannot be blank");
        }

        if (office.getCountry().length() > 50) {
            throw new IllegalArgumentException("Country max length is 50");
        }

        // Postal Code
        if (!StringUtils.hasText(office.getPostalCode())) {
            throw new IllegalArgumentException("Postal Code cannot be blank");
        }

        if (office.getPostalCode().length() > 15) {
            throw new IllegalArgumentException("Postal Code max length is 15");
        }

        // Territory
        if (!StringUtils.hasText(office.getTerritory())) {
            throw new IllegalArgumentException("Territory cannot be blank");
        }

        if (office.getTerritory().length() > 10) {
            throw new IllegalArgumentException("Territory max length is 10");
        }

        // Optional fields (no need to force)
        if (office.getState() != null && office.getState().length() > 50) {
            throw new IllegalArgumentException("State max length is 50");
        }

        if (office.getAddressLine2() != null && office.getAddressLine2().length() > 50) {
            throw new IllegalArgumentException("AddressLine2 max length is 50");
        }
    }
}