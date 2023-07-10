package com.example.usersservice.controllers;

import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.models.Transfer;
import com.example.usersservice.models.TransferMoneyResult;
import com.example.usersservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/operations")
public class BankController {

    private final UserService userService;

    @Autowired
    public BankController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferMoneyResult> transferMoney(@RequestBody Transfer transfer) throws TransferMoneyException, UserException {
        TransferMoneyResult tmr = userService.transferMoney(transfer.getToId(), transfer.getAmount());
        return new ResponseEntity<>(tmr, HttpStatus.OK);
    }

}
