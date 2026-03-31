package com.example.DemoCheck.controller;

import com.example.DemoCheck.dto.CustomerPatchDTO;
import com.example.DemoCheck.entity.Customer;
import com.example.DemoCheck.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PatchMapping("/update/{id}")
    public ResponseEntity<Customer> patchCustomer(
            @PathVariable Integer id,
            @RequestBody CustomerPatchDTO dto) {

        Customer updated = customerService.patchCustomer(id, dto);

        return ResponseEntity.ok(updated);
    }
}
