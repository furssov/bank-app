package com.example.mailsenderservice.gen;

import com.example.mailsenderservice.model.SecureCode;

public interface MessageGenerator {
    SecureCode generate(String email);
}
