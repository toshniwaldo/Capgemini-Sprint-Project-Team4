package com.example.DemoCheck.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/api")
    public String getResponse(){
        return "Hello from Demo controller";
    }
}
