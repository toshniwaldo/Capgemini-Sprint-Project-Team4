package com.example.DemoCheck.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name = "jobTitle", length = 50)
    private String jobTitle;

    @ManyToOne
    @JoinColumn(name = "reportsTo")
    @JsonIgnore   // ✅ ignore manager loop
    private Employee manager;

    @OneToMany(mappedBy = "manager")
    @JsonIgnore   // ✅ ignore manager loop
    private List<Employee> subordinates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officeCode")
    @JsonIgnore   // ✅ ignore office loop
    private Office office;

    @OneToMany(mappedBy = "salesRepEmployee")
    @JsonIgnore   // ✅ ignore customer loop
    private List<Customer> customers;

    @PrePersist
    @PreUpdate
    private void validateManager() {
        // If the employee has a manager, and the employee itself has an ID assigned
        if (this.manager != null && this.employeeNumber != null) {

            // Check if the IDs match
            if (this.employeeNumber.equals(this.manager.getEmployeeNumber())) {
                throw new IllegalStateException("JPA Rejection: An employee cannot report to themselves.");
            }
        }
    }
}