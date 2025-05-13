package com.example.contacts.config;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id="customContact")
public class CustomActuator {

    @ReadOperation
    public String contactStatus() {
        return "The contact app is running";
    }
}
