package com.example.Spring.Project_2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringProject2Controller {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }  
}

