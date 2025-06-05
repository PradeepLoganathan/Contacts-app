package com.example.contacts.api.services;

import com.example.contacts.api.models.Contact;
import com.example.contacts.repositories.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ContactService {

    private final ContactRepository repository;

    @Autowired
    public ContactService(ContactRepository repository) {
        this.repository = repository;
    }

    public List<Contact> findAll() {
        return repository.findAll();
    }

    public Contact create(Contact contact) {
        if (contact.getId() != null) {
            throw new IllegalArgumentException("ID must be null for new contact creation");
        }
        return repository.save(contact);
    }

    public Optional<Contact> findById(Long id) {
        return repository.findById(id);
    }

    public Contact update(Long id, Contact updatedContact) {
        return repository.findById(id)
                .map(contact -> {
                    contact.setName(updatedContact.getName());
                    contact.setEmail(updatedContact.getEmail());
                    contact.setPhone(updatedContact.getPhone());
                    return repository.save(contact);
                })
                .orElseThrow(() -> new IllegalArgumentException("Contact not found with id: " + id));
    }

    public Contact partialUpdate(Long id, Map<String, Object> updates) {
        return repository.findById(id)
                .map(contact -> {
                    updates.forEach((key, value) -> {
                        switch (key) {
                            case "name":
                                contact.setName((String) value);
                                break;
                            case "email":
                                contact.setEmail((String) value);
                                break;
                            case "phone":
                                contact.setPhone((String) value);
                                break;
                        }
                    });
                    return repository.save(contact);
                })
                .orElseThrow(() -> new IllegalArgumentException("Contact not found with id: " + id));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Contact not found with id: " + id);
        }
        repository.deleteById(id);
    }

    public boolean validateContact(Contact contact) {
        return contact.getEmail() != null && contact.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$" );
    }
}
