package com.example.usersservice.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BankExceptionHandler {

    @ExceptionHandler(value = BankException.class)
    public ResponseEntity<String> handler(BankException e) {
        return new ResponseEntity<>(e.getMessage(), e.getHttpStatus());
    }

}
