package com.example.DemoCheck.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

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

    @ManyToOne
    @JoinColumn(name = "officeCode")
    @JsonBackReference
    private Office office;

    @Column(name="reportsTo")
    private Integer reportsTo;

    @Column(name = "jobTitle", length = 50)
    private String jobTitle;

    @OneToMany(mappedBy = "salesRepEmployee")
    private List<Customer> customers;

}
