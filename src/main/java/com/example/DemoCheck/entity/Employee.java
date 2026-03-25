package com.example.DemoCheck.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="employees")
public class Employee {

    @Id
    @Column(name = "employeeNumber")
    private Integer employeeNumber;

    @Column(name = "lastName", nullable = false, length = 50)
    private String lastName;

    @Column(name = "firstName", nullable = false, length = 50)
    private String firstName;

    @Column(name = "extension", length = 10)
    private String extension;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "officeCode", nullable = false)
    private String officeCode;

    @Column(name = "jobTitle", length = 50)
    private String jobTitle;
}
