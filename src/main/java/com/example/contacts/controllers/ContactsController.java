package com.example.contacts.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

import com.example.contacts.models.Contact;
import com.example.contacts.services.ContactService;

import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
public class ContactsController {

    private final ContactService contactService;
    private final EntityManager em;
    private final RestTemplate rest;

    @Autowired
    public ContactsController(ContactService contactService, EntityManager em) {
        this.contactService = contactService;
        this.em = em;
        this.rest = new RestTemplate();
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

    // ===== INTENTIONALLY VULNERABLE ENDPOINTS =====
    // These endpoints are intentionally vulnerable for security testing purposes.
    // DO NOT use these patterns in production code!

    /**
     * VULNERABLE: SQL Injection
     * This endpoint is intentionally vulnerable to SQL injection attacks.
     * Example attack: ?q=' OR '1'='1
     * DO NOT use string concatenation for SQL queries in production!
     */
    @GetMapping("/vuln/search")
    public List<Contact> vulnerableSearch(@RequestParam("q") String q) {
        String sql = "SELECT * FROM contact WHERE name = '" + q + "'";
        Query nativeQuery = em.createNativeQuery(sql, Contact.class);
        return nativeQuery.getResultList();
    }

    /**
     * VULNERABLE: Command Injection
     * This endpoint is intentionally vulnerable to command injection attacks.
     * Example attack: ?cmd=cat /etc/passwd
     * DO NOT execute user input as commands in production!
     */
    @GetMapping("/vuln/exec")
    public String vulnerableExec(@RequestParam("cmd") String cmd) throws Exception {
        Process proc = Runtime.getRuntime().exec(cmd);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }
            return HtmlUtils.htmlEscape(out.toString());
        }
    }

    /**
     * VULNERABLE: Path Traversal
     * This endpoint is intentionally vulnerable to path traversal attacks.
     * Example attack: ?path=../../../etc/passwd
     * DO NOT use unvalidated user input for file paths in production!
     */
    @GetMapping("/vuln/read-file")
    public String vulnerableReadFile(@RequestParam("path") String path) throws Exception {
        byte[] allBytes = Files.readAllBytes(Paths.get(path));
        return new String(allBytes);
    }

    /**
     * VULNERABLE: Insecure Deserialization
     * This endpoint is intentionally vulnerable to deserialization attacks.
     * Example attack: Send a serialized malicious object
     * DO NOT deserialize untrusted data in production!
     */
    @PostMapping("/vuln/deserialize")
    public Object vulnerableDeserialize(@RequestBody byte[] payload) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload))) {
            return ois.readObject();
        }
    }

    /**
     * VULNERABLE: Server-Side Request Forgery (SSRF)
     * This endpoint is intentionally vulnerable to SSRF attacks.
     * Example attack: ?url=http://169.254.169.254/latest/meta-data/
     * DO NOT make requests to user-provided URLs in production!
     */
    @GetMapping("/vuln/fetch")
    public String vulnerableFetch(@RequestParam("url") String url) {
        return rest.getForObject(url, String.class);
    }

    /**
     * VULNERABLE: Cross-Site Scripting (XSS)
     * This endpoint is intentionally vulnerable to XSS attacks.
     * Example attack: ?msg=<script>alert('xss')</script>
     * DO NOT output unescaped user input in HTML in production!
     */
    @GetMapping(value = "/vuln/xss", produces = "text/html")
    @ResponseBody
    public String vulnerableXss(@RequestParam("msg") String msg) {
        return "<html><body><h1>You said:</h1><p>" + msg + "</p></body></html>";
    }
}
