package com.example.contacts.config;

import com.example.contacts.models.Contact;
import com.example.contacts.repositories.ContactRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration()
public class CustomTestConfiguration {
    @Bean
    CommandLineRunner initDatabase(ContactRepository repository) {

        return args -> {
            System.out.println("Preloading " + repository.save(new Contact("Jerry Smith", "123-456-7890", "jsmith@snyk.com")));
            System.out.println("Preloading " + repository.save(new Contact("Nancy Davis", "098-765-4321", "sdavis@snyk.com")));
        };
    }

}
