package com.example.usersservice.validators;

import com.example.usersservice.exceptions.CardReleaseException;
import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.gen.BankCardGenerator;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.CardCurrency;
import com.example.usersservice.models.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest
@ExtendWith(SpringExtension.class)
class BankCardValidatorImplTest {

    @Autowired
    private BankCardValidator bankCardValidator;

    @Autowired
    private BankCardGenerator bankCardGenerator;

    @Test
    void validateBankCard() throws CardReleaseException {
        List<BankCard> bankCards = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            BankCard bc = new BankCard();
            bc.setCardNumber(bankCardGenerator.generateBankCardNumber());
            bc.setCvv(bankCardGenerator.generateBankCardCvv());
            if (i % 2 == 0) {
                bc.setCardCurrency(CardCurrency.UAH);
            }
            else {
                bc.setCardCurrency(CardCurrency.EUR);
            }
            bankCards.add(bc);
        }
        User user = new User();

        BankCard existedCard = bankCards.get(32);
        user.setBankCards(List.of(existedCard));

        Assertions.assertFalse(bankCardValidator.validateUserBankCard(user, existedCard));

        BankCard notExisted = BankCard.builder()
                .cardNumber(bankCardGenerator.generateBankCardNumber())
                .cardCurrency(CardCurrency.UAH)
                 .cvv(bankCardGenerator.generateBankCardCvv())
                .build();

        CardReleaseException cre = Assertions.assertThrows( CardReleaseException.class, () ->{
            bankCardValidator.validateUserBankCard(user, notExisted);
        });
        String message = "You already have a "+ notExisted.getCardCurrency() +" card";
        Assertions.assertEquals(message, cre.getMessage());
        notExisted.setCardNumber(bankCardGenerator.generateBankCardNumber());
        notExisted.setCardCurrency(CardCurrency.USD);
        Assertions.assertTrue(bankCardValidator.validateUserBankCard(user, notExisted));

    }

    @Test
    void validateAmount() throws TransferMoneyException {
        for (int i = 1; i < 10000; i++) {
            Assertions.assertTrue(bankCardValidator.validateAmount(BigDecimal.valueOf(i + 1000), BigDecimal.valueOf(i)));
            Assertions.assertFalse(bankCardValidator.validateAmount(BigDecimal.valueOf(i), BigDecimal.valueOf(i + 1000)));
        }

        Assertions.assertFalse(bankCardValidator.validateAmount(BigDecimal.ZERO, BigDecimal.valueOf(500)));
        Assertions.assertFalse(bankCardValidator.validateAmount(BigDecimal.valueOf(498), BigDecimal.valueOf(499)));
        Assertions.assertTrue(bankCardValidator.validateAmount(BigDecimal.valueOf(500), BigDecimal.valueOf(500)));
        Assertions.assertFalse(bankCardValidator.validateAmount(BigDecimal.valueOf(500), BigDecimal.valueOf(-500)));
        Assertions.assertFalse(bankCardValidator.validateAmount(BigDecimal.valueOf(-500), BigDecimal.valueOf(-500)));
        Assertions.assertFalse(bankCardValidator.validateAmount(BigDecimal.valueOf(-500), BigDecimal.valueOf(500)));
        Assertions.assertTrue(bankCardValidator.validateAmount(BigDecimal.valueOf(499), BigDecimal.valueOf(498)));
    }

}