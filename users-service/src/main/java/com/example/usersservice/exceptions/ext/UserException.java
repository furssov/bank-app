package com.example.usersservice.exceptions.ext;

import com.example.usersservice.exceptions.BankException;
import org.springframework.http.HttpStatus;

public class UserException extends BankException {

    public UserException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

}
