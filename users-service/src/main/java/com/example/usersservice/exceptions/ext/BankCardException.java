package com.example.usersservice.exceptions.ext;

import com.example.usersservice.exceptions.BankException;

public class BankCardException extends BankException {
    public BankCardException(String message) {
        super(message);
    }
}
