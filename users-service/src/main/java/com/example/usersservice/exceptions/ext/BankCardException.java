package com.example.usersservice.exceptions.ext;

import com.example.usersservice.exceptions.BankException;
import org.springframework.http.HttpStatus;

public class BankCardException extends BankException {
    public BankCardException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
