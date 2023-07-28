package com.example.mailsenderservice.gen.Impl;

import com.example.mailsenderservice.gen.MessageGenerator;
import com.example.mailsenderservice.model.SecureCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MessageGeneratorImpl implements MessageGenerator {
    @Override
    public SecureCode generate(String email) {
        SecureCode secureCode = new SecureCode();
        secureCode.setReceiverEmail(email);
        secureCode.setSecureCode(email.hashCode() + UUID.randomUUID().toString());
        return secureCode;
    }
}
