package com.example.contacts.services;

import com.example.contacts.models.Contact;
import com.example.contacts.repositories.ContactRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContactServiceUnitTest {
    @Mock
    private ContactRepository repository;

    @InjectMocks
    private ContactService service;

    @Test
    public void testMockRepo() throws Exception {
        Contact c = new Contact();
        c.setId(1L);
        c.setName("John Doe");
        when(repository.findById(1L)).thenReturn(Optional.of(c));

        Contact result = service.getContactById(1L);
        assertEquals(c, result);
    }
}