package com.example.contacts.services;

import com.example.contacts.models.Contact;
import com.example.contacts.repositories.ContactRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContactService {
    @Autowired
    private ContactRepository repository;

    @Autowired
    private EmailService emailService;

    public Contact getContactById(Long id){
        return repository.findById(id).
                orElseThrow(() -> new EntityNotFoundException("Contact not found with id " + id));
    }

    public boolean validateContact(Contact c){
        return emailService.isEmailValid(c.getEmail());
    }
}
