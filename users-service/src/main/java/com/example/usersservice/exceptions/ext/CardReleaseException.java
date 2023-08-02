package com.example.usersservice.exceptions.ext;

import com.example.usersservice.exceptions.BankException;
import org.springframework.http.HttpStatus;

public class CardReleaseException extends BankException {
    public CardReleaseException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
