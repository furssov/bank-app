package com.example.usersservice.services;

import com.example.usersservice.exceptions.ext.CardReleaseException;
import com.example.usersservice.exceptions.ext.UserException;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.TransferMoneyResult;

import java.math.BigDecimal;

public interface BankService {
    TransferMoneyResult transferMoney(String senderCardNumber, BigDecimal amount, String receiverCardNumber) throws Throwable;

    boolean cardRelease(BankCard bankCard) throws UserException, CardReleaseException;

    BankCard findBankCardByCardNumber(String cardNumber) throws Throwable;

}
