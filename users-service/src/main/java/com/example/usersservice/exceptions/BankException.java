package com.example.usersservice.exceptions;

import org.springframework.http.HttpStatus;

public abstract class BankException extends RuntimeException{
    private HttpStatus httpStatus;

    public BankException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public BankException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
