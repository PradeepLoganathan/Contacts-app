package com.example.contacts.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/quality")
public class CodeQualityDemoController {

    // 1) MAGIC NUMBER, no constant defined
    @GetMapping("/discount")
    public double calculateDiscount(@RequestParam("amount") double amount) {
        return amount * 0.075; // what is 0.075?
    }

    // 2) DUPLICATED CODE across two methods
    @GetMapping("/dup1")
    public String duplicateMethod1(@RequestParam("input") String input) {
        String result = "";
        for (int i = 0; i < input.length(); i++) {
            result += Character.toUpperCase(input.charAt(i)); // inefficient String concat in loop
        }
        return result;
    }

    @GetMapping("/dup2")
    public String duplicateMethod2(@RequestParam("input") String input) {
        String result = "";
        for (int i = 0; i < input.length(); i++) {
            result += Character.toUpperCase(input.charAt(i));
        }
        return result;
    }

    // 3) EMPTY CATCH BLOCK — silently swallows IO errors
    @GetMapping("/read-file")
    public String readFile(@RequestParam("path") String path) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            // ignore
        }
        return sb.toString();
    }

    // 4) RESOURCE LEAK / Not closing BufferedReader
    @GetMapping("/count-lines")
    public int countLines(@RequestParam("path") String path) {
        int count = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            while (br.readLine() != null) {
                count++;
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return count;
    }

    // 5) THREAD.SLEEP in controller — blocking I/O on request thread
    @GetMapping("/wait")
    public String waitEndpoint() throws InterruptedException {
        Thread.currentThread().interrupt();
        Thread.sleep(5000);
        return "done";
    }

    // 6) TOO MANY PARAMETERS
    @GetMapping("/many-params")
    public String tooManyParams(@RequestParam("a") int a,
                                @RequestParam("b") int b,
                                @RequestParam("c") int c,
                                @RequestParam("d") int d,
                                @RequestParam("e") int e,
                                @RequestParam("f") int f) {
        return "sum=" + (a + b + c + d + e + f);
    }

    // 7) MUTABLE STATIC FIELD (thread-unsafe)
    private static Map<String, Integer> cache = new HashMap<>();

    @GetMapping("/cache")
    public int cacheExample(@RequestParam("key") String key) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        int value = key.length() * 42;
        cache.put(key, value);
        return value;
    }

    // 8) COMMENTED-OUT DEAD CODE / TODO left behind
    @GetMapping("/legacy")
    public String legacyMethod() {
        // TODO: re-implement this using new service
        // LegacyService.doSomething();
        return "not implemented";
    }

    // 9) MAGIC SECRET: hard-coded AWS credentials
    // Snyk will detect these as exposed secrets
    private static final String AWS_ACCESS_KEY_ID     = "AKIAEXAMPLEACCESSKEYID";
    private static final String AWS_SECRET_ACCESS_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    @GetMapping("/secrets")
    public String secretsDemo() {
        // returns the secret, but really just demonstrating the vuln
        return "accessKey=" + AWS_ACCESS_KEY_ID +
               "; secretKey=" + AWS_SECRET_ACCESS_KEY;
    }
}
