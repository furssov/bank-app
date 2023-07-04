package com.example.usersservice.exceptions;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(value = UserException.class)
    public ResponseEntity<String> handler(UserException e) {
        return new ResponseEntity<>(e.getMessage(), e.getHttpStatus());
    }
}
