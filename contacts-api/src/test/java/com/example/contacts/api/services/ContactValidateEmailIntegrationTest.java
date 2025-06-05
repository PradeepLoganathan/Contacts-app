package com.example.contacts.api.services;

import com.example.contacts.api.services.ContactService;

import com.example.contacts.api.models.Contact;
import com.example.contacts.config.CustomTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(CustomTestConfiguration.class)
@ActiveProfiles("test")
public class ContactValidateEmailIntegrationTest {
    @Autowired
    private ContactService service;

    @Test
    public void testIntegration() throws Exception {
        Contact c = new Contact();
        c.setId(1L);
        c.setName("John Doe");
        c.setEmail("john@doe.com");
        c.setPhone("+1234567890");  // Add required phone field

        boolean result = service.validateContact(c);
        assertTrue(result);
    }
}