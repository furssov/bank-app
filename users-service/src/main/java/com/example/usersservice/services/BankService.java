package com.example.usersservice.services;

import com.example.usersservice.exceptions.CardReleaseException;
import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.TransferMoneyResult;

import java.math.BigDecimal;

public interface BankService {
    TransferMoneyResult transferMoney(String senderCardNumber, BigDecimal amount, String receiverCardNumber) throws Throwable;

    boolean cardRelease(BankCard bankCard) throws UserException, CardReleaseException;

    BankCard findBankCardByCardNumber(String cardNumber) throws Throwable;

}
