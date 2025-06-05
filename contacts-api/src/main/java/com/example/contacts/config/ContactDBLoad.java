package com.example.contacts.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.contacts.api.models.Contact;
import com.example.contacts.repositories.ContactRepository;

@Configuration
public class ContactDBLoad {
    @Bean
    CommandLineRunner initDatabase(ContactRepository repository) {

        return args -> {
            System.out.println("Preloading " + repository.save(new Contact("John Smith", "+61412345678", "jsmith@snyk.com")));
            System.out.println("Preloading " + repository.save(new Contact("Samantha Davis", "+61487654321", "sdavis@snyk.com")));
            };
    }

}
