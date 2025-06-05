package com.example.contacts.controllers;

import com.example.contacts.api.models.Contact;
import com.example.contacts.repositories.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.client.RestTemplate;

import javax.naming.Context;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/adv")
public class AdvancedVulnController {

    @Autowired
    private ContactRepository repository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private RestTemplate rest;

    /**
     * 1) XML External Entity (XXE) Vulnerability
     */
    @PostMapping(value = "/vuln/xxe", consumes = "application/xml")
    public String vulnerableXxe(@RequestBody String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        return doc.getDocumentElement().getTextContent();
    }

    /**
     * 2) SpEL Injection
     */
    @GetMapping("/vuln/spel")
    public String vulnerableSpel(@RequestParam("exp") String exp) {
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(exp);
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("repo", repository);
        return expression.getValue(ctx, String.class);
    }

    /**
     * 3) JNDI Injection
     */
    @GetMapping("/vuln/jndi")
    public Object vulnerableJndi(@RequestParam("name") String name) throws Exception {
        Context ctx = new InitialContext();
        return ctx.lookup(name);
    }

    /**
     * 4) Unsafe Reflection
     */
    @GetMapping("/vuln/reflect")
    public Object vulnerableReflect(@RequestParam("className") String className) throws Exception {
        Class<?> clazz = Class.forName(className);
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * VULNERABLE: SQL Injection
     */
    @GetMapping("/vuln/search")
    public List<Contact> vulnerableSearch(@RequestParam("q") String q) {
        String sql = "SELECT * FROM contact WHERE name = '" + q + "'";
        Query nativeQuery = em.createNativeQuery(sql, Contact.class);
        return nativeQuery.getResultList();
    }

    /**
     * VULNERABLE: Command Injection
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
     */
    @GetMapping("/vuln/read-file")
    public String vulnerableReadFile(@RequestParam("path") String path) throws Exception {
        byte[] allBytes = Files.readAllBytes(Paths.get(path));
        return new String(allBytes);
    }

    /**
     * VULNERABLE: Insecure Deserialization
     */
    @PostMapping("/vuln/deserialize")
    public Object vulnerableDeserialize(@RequestBody byte[] payload) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload))) {
            return ois.readObject();
        }
    }

    /**
     * VULNERABLE: Server-Side Request Forgery (SSRF)
     */
    @GetMapping("/vuln/fetch")
    public String vulnerableFetch(@RequestParam("url") String url) {
        return rest.getForObject(url, String.class);
    }

    /**
     * VULNERABLE: Cross-Site Scripting (XSS)
     */
    @GetMapping(value = "/vuln/xss", produces = "text/html")
    @ResponseBody
    public String vulnerableXss(@RequestParam("msg") String msg) {
        return "<html><body><h1>You said:</h1><p>" + msg + "</p></body></html>";
    }
}
