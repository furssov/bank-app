package com.example.usersservice.validators;

import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.CardCurrency;

import java.math.BigDecimal;
import java.util.List;

public interface BankCardValidator {
    boolean validateBankCard(List<BankCard> bankCards, String card, CardCurrency cardCurrency, String cvv);
    boolean validateAmount(BigDecimal money, BigDecimal amount);
}
