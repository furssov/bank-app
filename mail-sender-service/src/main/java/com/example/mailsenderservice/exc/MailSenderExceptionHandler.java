package com.example.mailsenderservice.exc;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MailSenderExceptionHandler {

    @ExceptionHandler(value = SecureCodeException.class)
    public ResponseEntity<String> handle(SecureCodeException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
