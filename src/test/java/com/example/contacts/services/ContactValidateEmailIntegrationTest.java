package com.example.contacts.services;

import com.example.contacts.models.Contact;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ContactValidateEmailIntegrationTest {
    @Autowired
    private ContactService service;

    @Test
    public void testIntegration() throws Exception {
        Contact c = new Contact();
        c.setId(1L);
        c.setName("John Doe");
        c.setEmail("john@doe.com");

        boolean result = service.validateContact(c);
        assertTrue(result);

    }
}