package com.example.contacts.services;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public boolean isEmailValid(String email){
        return email.contains("@");
    }
}
