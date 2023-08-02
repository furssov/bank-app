package com.example.usersservice.controllers;

import com.example.usersservice.dto.BankCardDto;
import com.example.usersservice.exceptions.ext.CardReleaseException;
import com.example.usersservice.mappers.BankMapper;
import com.example.usersservice.exceptions.ext.UserException;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.Transfer;
import com.example.usersservice.models.TransferMoneyResult;
import com.example.usersservice.services.BankService;
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

    private final BankService bankService;

    private final BankMapper bankMapper;

    @Autowired
    public BankController(@Qualifier("bankServiceImpl") BankService bankService, @Qualifier("cardMapper") BankMapper bankMapper) {
        this.bankService = bankService;
        this.bankMapper = bankMapper;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferMoneyResult> transferMoney(@RequestBody Transfer transfer) throws Throwable {
        TransferMoneyResult tmr = bankService.transferMoney(transfer.getFromCard(), transfer.getAmount(), transfer.getToCard());
        return new ResponseEntity<>(tmr, HttpStatus.OK);
    }

    @PostMapping("/bank-card")
    public ResponseEntity createBankCard(@RequestBody @Valid BankCardDto bankCardDto, Errors errors) throws CardReleaseException, UserException {
        if (errors.hasErrors()) {
            return new ResponseEntity(errors.getAllErrors().get(0).getDefaultMessage(), HttpStatus.BAD_REQUEST);
        }
        return bankService.cardRelease((BankCard) bankMapper.map(bankCardDto)) ? new ResponseEntity(HttpStatus.OK) : new ResponseEntity(HttpStatus.BAD_REQUEST);

    }

}
