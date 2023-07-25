package com.example.usersservice.validators;

import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.CardCurrency;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class BankCardValidatorImpl implements BankCardValidator{
    @Override
    public boolean validateBankCard(List<BankCard> bankCards, String card, CardCurrency cardCurrency, String cvv) {
        Optional<BankCard> bk = bankCards.stream()
                .filter(bankCard -> bankCard.getCardNumber().equals(card) && bankCard.getCardCurrency().equals(cardCurrency) && bankCard.getCvv().equals(cvv))
                .findAny();
        if (bk.isPresent()) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public boolean validateAmount(BigDecimal money, BigDecimal amount) {
        if (money.subtract(amount).compareTo(BigDecimal.ZERO) >= 0 && amount.compareTo(BigDecimal.ZERO) > 0) {
            return true;
        }
        else {
            return false;
        }
    }
}
