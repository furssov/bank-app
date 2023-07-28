package com.example.mailsenderservice.controller;

import com.example.mailsenderservice.exc.SecureCodeException;
import com.example.mailsenderservice.gen.MessageGenerator;
import com.example.mailsenderservice.model.SecureCode;
import com.example.mailsenderservice.service.MailSender;
import com.example.mailsenderservice.service.SecureCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/security-code")
public class MailController {

    private static final String CODE_MESSAGE = "Hi! It's your security code, don't forget it : ";
    private final MailSender mailSender;
    private final SecureCodeService service;

    private final MessageGenerator messageGenerator;

    @Autowired
    public MailController(MailSender mailSender, SecureCodeService service, MessageGenerator messageGenerator) {
        this.mailSender = mailSender;
        this.service = service;
        this.messageGenerator = messageGenerator;
    }

    @PostMapping("/send/to/{email}")
    public ResponseEntity<SecureCode> sendMail(@PathVariable String email) {
        SecureCode secureCode = messageGenerator.generate(email);
        mailSender.sendMessage(email, "Security Code", CODE_MESSAGE + secureCode.getSecureCode());
        service.save(secureCode);
        return new ResponseEntity(secureCode, HttpStatus.OK);
    }

    @GetMapping("/{email}")
    public ResponseEntity<SecureCode> getCodeByEmail(@PathVariable String email) throws SecureCodeException {
        return new ResponseEntity<>(service.findByReceiverEmail(email), HttpStatus.OK);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity deleteSecureCode(@PathVariable String email) {
        service.deleteSecureCodeByEmail(email);
        return new ResponseEntity(HttpStatus.OK);
    }
}
