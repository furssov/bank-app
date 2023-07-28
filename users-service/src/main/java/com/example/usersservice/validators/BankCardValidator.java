package com.example.usersservice.validators;

import com.example.usersservice.exceptions.CardReleaseException;
import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.gen.BankCardGenerator;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.CardCurrency;
import com.example.usersservice.models.User;

import java.math.BigDecimal;
import java.util.List;

public interface BankCardValidator {
    boolean validateUserBankCard(User user, BankCard bankCard) throws CardReleaseException;
    boolean validateAmount(BigDecimal money, BigDecimal amount) throws TransferMoneyException;
}
