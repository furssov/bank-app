package com.example.mailsenderservice.controller;

import com.example.mailsenderservice.exc.SecureCodeException;
import com.example.mailsenderservice.model.SecureCode;
import com.example.mailsenderservice.service.MailSender;
import com.example.mailsenderservice.service.SecureCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/security-code")
public class MailController {

    private static final String CODE_MESSAGE = "Hi! It's your security code, don't forget it : ";
    private final MailSender mailSender;
    private final SecureCodeService service;

    @Autowired
    public MailController(MailSender mailSender, SecureCodeService service) {
        this.mailSender = mailSender;
        this.service = service;
    }

    @GetMapping("/send/to/{email}")
    public ResponseEntity<SecureCode> sendMail(@PathVariable String email) {
        SecureCode secureCode = new SecureCode();
        secureCode.setReceiverEmail(email);
        String code = email.hashCode() + UUID.randomUUID().toString();
        secureCode.setSecureCode(code);
        secureCode.setExpiration(600L);
        mailSender.sendMessage(email, "Security Code", CODE_MESSAGE + code);
        service.saveSecureCode(secureCode);
        return new ResponseEntity(secureCode, HttpStatus.OK);
    }

    @GetMapping("/{email}")
    public ResponseEntity<SecureCode> getCodeByEmail(@PathVariable String email) throws SecureCodeException {
        return new ResponseEntity<>(service.findSecureCodeByReceiverEmail(email), HttpStatus.OK);
    }
}
