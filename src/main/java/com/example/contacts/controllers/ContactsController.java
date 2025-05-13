package com.example.contacts.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.contacts.models.Contact;
import com.example.contacts.repositories.ContactRepository;

import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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
            return out.toString();
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
        return rest.getForObject(url, String.class);
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
