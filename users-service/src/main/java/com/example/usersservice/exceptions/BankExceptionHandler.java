package com.example.usersservice.exceptions;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class BankExceptionHandler {

    @ExceptionHandler(value = {UserException.class, BankException.class})
    public ResponseEntity<String> handler(BankException e) {
        return new ResponseEntity<>(e.getMessage(), e.getHttpStatus());
    }
}
