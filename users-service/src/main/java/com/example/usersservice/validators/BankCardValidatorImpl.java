package com.example.usersservice.validators;

import com.example.usersservice.exceptions.CardReleaseException;
import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.gen.BankCardGenerator;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.CardCurrency;
import com.example.usersservice.models.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class BankCardValidatorImpl implements BankCardValidator{
    @Override
    public boolean validateUserBankCard(User user, BankCard bc) throws CardReleaseException {
        List<BankCard> bankCards = user.getBankCards();
        Optional<BankCard> bk = bankCards.stream()
                .filter(bankCard -> bankCard.getCardNumber().equals(bc.getCardNumber()))
                .findAny();
        if (bk.isPresent()) {
            return false;
        }
        else {
            if (bankCards.stream().anyMatch(bankCard -> {
                return bankCard.getCardCurrency().equals(bc.getCardCurrency());
            })) {
                throw new CardReleaseException("You already have a "+ bc.getCardCurrency() +" card");
            }
            return true;
        }
    }

    @Override
    public boolean validateAmount(BigDecimal money, BigDecimal amount) throws TransferMoneyException {
        if (money.subtract(amount).compareTo(BigDecimal.ZERO) >= 0 && amount.compareTo(BigDecimal.ZERO) > 0) {
            return true;
        }
        return false;
    }
}
