package com.example.usersservice.validators;

import com.example.usersservice.exceptions.ext.CardReleaseException;
import com.example.usersservice.exceptions.ext.TransferMoneyException;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.User;

import java.math.BigDecimal;

public interface BankCardValidator {
    boolean validateUserBankCard(User user, BankCard bankCard) throws CardReleaseException;
    boolean validateAmount(BigDecimal money, BigDecimal amount) throws TransferMoneyException;
}
