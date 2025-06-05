package com.example.contacts.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @Value("${spring.application.version}")
    private String version;

    @GetMapping("/")
    String root() {
        return "Status: Running, version: " + version;
    }
}
