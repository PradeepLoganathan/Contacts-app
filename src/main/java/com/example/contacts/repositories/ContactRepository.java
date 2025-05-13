package com.example.contacts.repositories;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.contacts.models.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
