package com.example.usersservice.controllers;

import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.models.TransferAmount;
import com.example.usersservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/bank/operations")
public class BankController {

    private final UserService userService;

    @Autowired
    public BankController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/from/{fromId}/to/{toId}")
    public ResponseEntity transferMoney(@PathVariable String fromId, @PathVariable String toId, @RequestBody TransferAmount amount) throws TransferMoneyException, UserException {
        userService.transferMoney(fromId, toId, amount.getAmount());
        return new ResponseEntity(HttpStatus.OK);
    }

}
