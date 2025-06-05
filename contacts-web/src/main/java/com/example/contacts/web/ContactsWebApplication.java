package com.example.contacts.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.contacts.web", "com.example.contacts.api", "com.example.contacts.repositories"})
@EnableJpaRepositories(basePackages = "com.example.contacts.repositories")
@EntityScan(basePackages = "com.example.contacts.api.models")
public class ContactsWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContactsWebApplication.class, args);
    }
} 