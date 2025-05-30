package com.example.contacts.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.example.contacts.models.Contact;
import com.example.contacts.repositories.ContactRepository;

import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid; 

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import org.apache.commons.text.StringEscapeUtils;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class ContactsController {

    @Autowired
    private ContactRepository repository;

    @Autowired
    private EntityManager em;

    private final RestTemplate rest = new RestTemplate();

    @GetMapping("/contacts")
    List<Contact> all() {
        return repository.findAll();
    }

    /**
     * POST /contacts : Create a new contact.
     * @param newContact The contact to create.
     * @return The created contact with HTTP status 201 (Created).
     */
    @PostMapping("/contacts")
    public ResponseEntity<Contact> createContact(@Valid @RequestBody Contact newContact) {
        // Ensure ID is null so that the database generates it
        if (newContact.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must be null for new contact creation.");
        }
        Contact savedContact = repository.save(newContact);
        return new ResponseEntity<>(savedContact, HttpStatus.CREATED);
    }

    /**
     * GET /contacts/{id} : Get a specific contact by its ID.
     * @param id The ID of the contact.
     * @return The contact if found, otherwise HTTP status 404 (Not Found).
     */
    @GetMapping("/contacts/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable Long id) {
        return repository.findById(id)
                .map(contact -> ResponseEntity.ok(contact))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /contacts/{id} : Update an existing contact or create it if it doesn't exist (upsert).
     * More commonly, PUT is used for full replacement of an existing resource.
     * @param id The ID of the contact to update.
     * @param updatedContact The updated contact data.
     * @return The updated contact with HTTP status 200 (OK) or 201 (Created) if new.
     */
    @PutMapping("/contacts/{id}")
    public ResponseEntity<Contact> updateContact(@PathVariable Long id, @Valid @RequestBody Contact updatedContact) {
        return repository.findById(id)
                .map(contact -> {
                    contact.setName(updatedContact.getName());
                    contact.setEmail(updatedContact.getEmail());
                    contact.setPhone(updatedContact.getPhone());
                    // Set other fields as necessary
                    Contact savedContact = repository.save(contact);
                    return ResponseEntity.ok(savedContact);
                })
                .orElseGet(() -> { // If contact with ID not found, create a new one (typical PUT behavior)
                    // It's often debated if PUT should create if not exists.
                    // For this example, we'll allow creation if ID is provided in path but not in DB.
                    // Ensure the ID from the path is set on the new contact object.
                    updatedContact.setId(id); // Or handle as an error if strict update-only is desired
                    Contact savedContact = repository.save(updatedContact);
                    return new ResponseEntity<>(savedContact, HttpStatus.CREATED);
                });
    }
    
    /**
     * PATCH /contacts/{id} : Partially update an existing contact.
     * @param id The ID of the contact to update.
     * @param updates A map of fields to update.
     * @return The updated contact with HTTP status 200 (OK), or 404 (Not Found).
     */
    @PatchMapping("/contacts/{id}")
    public ResponseEntity<Contact> partiallyUpdateContact(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
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
                            // Add other fields as necessary
                        }
                    });
                    Contact savedContact = repository.save(contact);
                    return ResponseEntity.ok(savedContact);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    /**
     * DELETE /contacts/{id} : Delete a specific contact by its ID.
     * @param id The ID of the contact to delete.
     * @return HTTP status 204 (No Content) if successful, or 404 (Not Found).
     */
    @DeleteMapping("/contacts/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * VERY VULNERABLE: SQL built via string concat.
     * Snyk will detect "SQL injection via concatenation".
     */
    @GetMapping("/vuln/search")
    public List<Contact> vulnerableSearch(@RequestParam("q") String q) {
        String sql = "SELECT * FROM contact WHERE name = '" + q + "'";
        Query nativeQuery = em.createNativeQuery(sql, Contact.class);
        return nativeQuery.getResultList();
    }
    
    /**
     * VERY VULNERABLE: executes whatever command the user passes.
     * Snyk will detect "OS command injection / RCE" in this method.
     */
    @GetMapping("/vuln/exec")
    public String vulnerableExec(@RequestParam("cmd") String cmd) throws Exception {
        // directly pass user input to Runtime.exec()
        Process proc = Runtime.getRuntime().exec(cmd);

        // capture stdout
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream()))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }
            return StringEscapeUtils.escapeHtml4(out.toString());
        }
    }

    /**
     * VULNERABLE: Reads any file on disk based on unvalidated user input.
     * Snyk will flag "Path Traversal" here.
     */
    @GetMapping("/vuln/read-file")
    public String vulnerableReadFile(@RequestParam("path") String path) throws Exception {
        // e.g. someone calls: ?path=../../../../etc/passwd
        byte[] allBytes = Files.readAllBytes(Paths.get(path));
        return new String(allBytes);
    }

    /**
     * VULNERABLE: Deserializes raw byte[] from the client.
     * Snyk will flag "Insecure Deserialization".
     */
    @PostMapping("/vuln/deserialize")
    public Object vulnerableDeserialize(@RequestBody byte[] payload) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload))) {
            return ois.readObject();
        }
    }
    
    /**
     * VULNERABLE: Fetches whatever URL the user passes in.
     * Snyk will flag "SSRF" here.
     */
    @GetMapping("/vuln/fetch")
    public String vulnerableFetch(@RequestParam("url") String url) {
        // e.g. ?url=http://169.254.169.254/latest/meta-data/
        return StringEscapeUtils.escapeHtml4(rest.getForObject(url, String.class));
    }

    /**
     * VULNERABLE: Echoes unescaped input into an HTML context.
     * Snyk will flag "Reflected XSS".
     */
    @GetMapping(value = "/vuln/xss", produces = "text/html")
    @ResponseBody
    public String vulnerableXss(@RequestParam("msg") String msg) {
        return "<html><body><h1>You said:</h1><p>" + msg + "</p></body></html>";
    }
}
