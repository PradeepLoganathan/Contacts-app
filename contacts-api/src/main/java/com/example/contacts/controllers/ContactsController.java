package com.example.contacts.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.contacts.api.models.Contact;
import com.example.contacts.api.services.ContactService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
public class ContactsController {

    private final ContactService contactService;

    @Autowired
    public ContactsController(ContactService contactService) {
        this.contactService = contactService;
    }

    // Secure endpoints
    @GetMapping("/contacts")
    List<Contact> all() {
        return contactService.findAll();
    }

    @PostMapping("/contacts")
    public ResponseEntity<Contact> createContact(@Valid @RequestBody Contact newContact) {
        Contact savedContact = contactService.create(newContact);
        return new ResponseEntity<>(savedContact, HttpStatus.CREATED);
    }

    @GetMapping("/contacts/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable Long id) {
        return contactService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/contacts/{id}")
    public ResponseEntity<Contact> updateContact(@PathVariable Long id, @Valid @RequestBody Contact updatedContact) {
        try {
            Contact savedContact = contactService.update(id, updatedContact);
            return ResponseEntity.ok(savedContact);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/contacts/{id}")
    public ResponseEntity<Contact> partiallyUpdateContact(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            Contact savedContact = contactService.partialUpdate(id, updates);
            return ResponseEntity.ok(savedContact);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/contacts/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        try {
            contactService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    
}
