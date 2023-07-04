package com.example.usersservice.exceptions;

import org.springframework.http.HttpStatus;

public abstract class BankException extends Exception{
    private HttpStatus httpStatus;

    public BankException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
