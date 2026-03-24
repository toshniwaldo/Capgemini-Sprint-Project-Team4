package com.example.DemoCheck.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World from Tanishq Sawarkar";
    }

    @GetMapping("/hellosanchit")
    public String helloSanchit() {
        return "Hello World from Sanchit Pahurkar";
    }
    
}
