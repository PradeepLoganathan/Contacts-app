package com.example.contacts.controllers;

import com.example.contacts.models.Contact;
import com.example.contacts.repositories.ContactRepository;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

@RestController
@RequestMapping("/adv")
public class AdvancedVulnController {

    @Autowired
    private ContactRepository repository;

    /**
     * 1) XML External Entity (XXE) Vulnerability
     *    Parses raw XML without disabling external entities.
     *    Snyk will flag "XXE" here.
     */
    @PostMapping(value = "/vuln/xxe", consumes = "application/xml")
    public String vulnerableXxe(@RequestBody String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // ⚠️ External entities still enabled by default
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        return doc.getDocumentElement().getTextContent();
    }

    /**
     * 2) SpEL Injection
     *    Evaluates an arbitrary Spring Expression Language string.
     *    Snyk will flag "SpEL injection" here.
     */
    @GetMapping("/vuln/spel")
    public String vulnerableSpel(@RequestParam("exp") String exp) {
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(exp);
        // Expose the repository into the SpEL context
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("repo", repository);
        // Now user can call: ?exp=@repo.findAll()
        return expression.getValue(ctx, String.class);
    }

    /**
     * 3) JNDI Injection
     *    Performs a lookup on a user-supplied JNDI name.
     *    Snyk will flag "JNDI injection" here.
     */
    @GetMapping("/vuln/jndi")
    public Object vulnerableJndi(@RequestParam("name") String name) throws Exception {
        Context ctx = new InitialContext();
        // 🔥 Dangerous: user can point to ldap://… or rmi://… and trigger remote class loading
        return ctx.lookup(name);
    }

    /**
     * 4) Unsafe Reflection
     *    Instantiates any class by name.
     *    Snyk will flag "Unsafe Reflection" here.
     */
    @GetMapping("/vuln/reflect")
    public Object vulnerableReflect(@RequestParam("className") String className) throws Exception {
        // e.g. ?className=java.lang.Runtime
        Class<?> clazz = Class.forName(className);
        return clazz.getDeclaredConstructor().newInstance();
    }
}
