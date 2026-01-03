package com.example.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BackendController {

    @GetMapping("/hello")
    public String hello() {
        System.out.println("Hello World 1");
        return "Hello from BACKEND 1";
    }
}
