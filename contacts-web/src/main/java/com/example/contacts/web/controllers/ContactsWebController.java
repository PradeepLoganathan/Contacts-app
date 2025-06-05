package com.example.contacts.web.controllers;

import com.example.contacts.api.models.Contact;
import com.example.contacts.api.services.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/contacts")
public class ContactsWebController {

    private final ContactService contactService;

    @Autowired
    public ContactsWebController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public String listContacts(Model model) {
        model.addAttribute("contacts", contactService.findAll());
        model.addAttribute("newContact", new Contact());
        return "contacts/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("contact", new Contact());
        return "contacts/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Contact contact = contactService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid contact Id:" + id));
        model.addAttribute("contact", contact);
        return "contacts/form";
    }

    @PostMapping
    public String saveContact(@ModelAttribute Contact contact) {
        contactService.create(contact);
        return "redirect:/contacts";
    }

    @PostMapping("/{id}")
    public String updateContact(@PathVariable Long id, @ModelAttribute Contact contact) {
        contactService.update(id, contact);
        return "redirect:/contacts";
    }

    @PostMapping("/delete/{id}")
    public String deleteContact(@PathVariable Long id) {
        contactService.delete(id);
        return "redirect:/contacts";
    }
} 