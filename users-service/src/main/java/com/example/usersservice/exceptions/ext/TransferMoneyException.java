package com.example.usersservice.exceptions.ext;

import com.example.usersservice.exceptions.BankException;
import org.springframework.http.HttpStatus;

public class TransferMoneyException extends BankException {

    public TransferMoneyException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

    public TransferMoneyException(String message) {super(message);}

}
