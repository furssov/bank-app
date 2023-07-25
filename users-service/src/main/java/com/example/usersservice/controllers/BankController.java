package com.example.usersservice.controllers;

import com.example.usersservice.dto.BankCardDto;
import com.example.usersservice.mappers.BankMapper;
import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.Transfer;
import com.example.usersservice.models.TransferMoneyResult;
import com.example.usersservice.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/operations")
public class BankController {

    private final UserService userService;

    private final BankMapper bankMapper;

    @Autowired
    public BankController(@Qualifier("userServiceImpl") UserService userService, @Qualifier("cardMapper") BankMapper bankMapper) {
        this.userService = userService;
        this.bankMapper = bankMapper;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferMoneyResult> transferMoney(@RequestBody Transfer transfer) throws TransferMoneyException, UserException {
        TransferMoneyResult tmr = userService.transferMoney(transfer.getFromCard(), transfer.getAmount(), transfer.getToCard());
        return new ResponseEntity<>(tmr, HttpStatus.OK);
    }

    @PostMapping("/bank-card")
    public ResponseEntity createBankCard(@RequestBody @Valid BankCardDto bankCardDto, Errors errors) {
        if (errors.hasErrors()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return userService.cardRelease((BankCard) bankMapper.map(bankCardDto)) ? new ResponseEntity(HttpStatus.OK) : new ResponseEntity(HttpStatus.BAD_REQUEST);

    }

}
