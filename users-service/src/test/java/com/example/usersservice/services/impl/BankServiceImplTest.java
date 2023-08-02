package com.example.usersservice.services.impl;

import com.example.usersservice.exceptions.ext.CardReleaseException;
import com.example.usersservice.exceptions.ext.UserException;
import com.example.usersservice.feigns.TransferMoneyProxyService;
import com.example.usersservice.gen.BankCardGenerator;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.CardCurrency;
import com.example.usersservice.models.TransferMoneyResult;
import com.example.usersservice.models.User;
import com.example.usersservice.repos.BankRepository;
import com.example.usersservice.repos.UserRepository;
import com.example.usersservice.services.BankService;
import com.springboot.conversion.beans.CurrencyConversionBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.*;

@SpringBootTest
class BankServiceImplTest {

    @TestConfiguration
    static class BankServiceBean {
        @Bean
        public BankService bankTestBean() {
            return new BankServiceImpl();
        }
    }

    @Autowired
    @Qualifier("bankTestBean")
    private BankService bankService;

    @MockBean
    private BankCardGenerator bankCardGenerator;

    @MockBean
    private BankRepository bankRepository;

    @MockBean
    private TransferMoneyProxyService transferMoneyProxyService;

    @MockBean
    private Authentication authentication;

    @MockBean
    private UserRepository userRepository;

    @Test
    void transferMoney() throws Throwable {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Mockito.when(bankCardGenerator.generateBankCardNumber()).thenReturn("12345");
        String fromCard = bankCardGenerator.generateBankCardNumber();
        Mockito.when(bankCardGenerator.generateBankCardNumber()).thenReturn("12435");
        String toCard = bankCardGenerator.generateBankCardNumber();
        BigDecimal amount = BigDecimal.valueOf(150L);


        BankCard fromBankCard = BankCard.builder()
                .cardNumber(fromCard)
                .cardCurrency(CardCurrency.EUR)
                .amount(amount)
                .build();

        BankCard toBankCard = BankCard.builder()
                .cardNumber(toCard)
                .cardCurrency(CardCurrency.EUR)
                .amount(amount)
                .build();

        String fromUserId = UUID.randomUUID().toString();
        User fromUser = new User.UserBuilder()
                .withId(fromUserId)
                .withBankCards(List.of(fromBankCard))
                .withFirstName("Andrii")
                .build();

        fromBankCard.setUser(fromUser);

        String toUserId = UUID.randomUUID().toString();
        User toUser = new User.UserBuilder()
                .withId(toUserId)
                .withBankCards(List.of(toBankCard))
                .withFirstName("Julia")
                .build();

        toBankCard.setUser(toUser);

        Mockito.when(authentication.getName()).thenReturn(fromUserId);
        Mockito.when(userRepository.findById(fromUserId)).thenReturn(Optional.of(fromUser));
        Mockito.when(bankRepository.findBankCardByCardNumber(toCard)).thenReturn(Optional.of(toBankCard));
        CurrencyConversionBean ccb = new CurrencyConversionBean(1l,
                CardCurrency.EUR.name(), CardCurrency.EUR.name(), BigDecimal.valueOf(1), BigDecimal.valueOf(150), BigDecimal.valueOf(150), 0);
        Mockito.when(transferMoneyProxyService.getResultOfConversion(fromBankCard.getCardCurrency().name(),
                toBankCard.getCardCurrency().name(), amount)).thenReturn(ccb);
        Mockito.when(bankRepository.save(fromBankCard)).thenReturn(fromBankCard);
        Mockito.when(bankRepository.save(toBankCard)).thenReturn(toBankCard);

        TransferMoneyResult result = new TransferMoneyResult(fromBankCard.getCardNumber(), fromUser.getFirstName(), fromUser.getSecondName(),
                toBankCard.getCardNumber(), toUser.getFirstName(), toUser.getSecondName(), ccb.getTotalAmount(), toBankCard.getCardCurrency().name());

        Assertions.assertEquals(result, bankService.transferMoney(fromCard, amount, toCard));

        Assertions.assertEquals(BigDecimal.valueOf(0), fromBankCard.getAmount());
        Assertions.assertEquals(BigDecimal.valueOf(300), toBankCard.getAmount());

        fromBankCard.setCardCurrency(CardCurrency.USD);
        fromBankCard.setAmount(BigDecimal.valueOf(500));
        amount = BigDecimal.valueOf(200);
        ccb = new CurrencyConversionBean(2l, CardCurrency.USD.name(), CardCurrency.EUR.name(), /*from db */BigDecimal.valueOf(0.92), amount,
                BigDecimal.valueOf(0.92).multiply(amount), 0);
        result = new TransferMoneyResult(fromBankCard.getCardNumber(), fromUser.getFirstName(), fromUser.getSecondName(),
                toBankCard.getCardNumber(), toUser.getFirstName(), toUser.getSecondName(), ccb.getTotalAmount(), toBankCard.getCardCurrency().name());
        Mockito.when(transferMoneyProxyService.getResultOfConversion(fromBankCard.getCardCurrency().name(),
                toBankCard.getCardCurrency().name(), amount)).thenReturn(ccb);
        Assertions.assertEquals(result, bankService.transferMoney(fromCard, amount, toCard));
    }

    @Test
    void cardRelease() throws CardReleaseException, UserException {
        User user = new User();
        String id = UUID.randomUUID().toString();
        user.setId(id);
        user.setBankCards(new ArrayList<>());
        Mockito.when(authentication.getName()).thenReturn(id);
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
        Mockito.when(bankRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
        Mockito.when(bankCardGenerator.generateBankCardNumber()).thenReturn("12345");

        BankCard bankCard = BankCard.builder()
                .cardCurrency(CardCurrency.EUR)
                .pinCode("1234")
                .cardNumber(bankCardGenerator.generateBankCardNumber())
                .build();

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Assertions.assertTrue(bankService.cardRelease(bankCard));
        Assertions.assertEquals(1, user.getBankCards().size());
        BankCard bankCardWithTheSameCardNum = BankCard.builder()
                .cardCurrency(CardCurrency.UAH)
                .pinCode("1234")
                .cardNumber(bankCard.getCardNumber())
                .build();
          CardReleaseException cre =  Assertions.assertThrows(CardReleaseException.class, () -> {
              bankService.cardRelease(bankCardWithTheSameCardNum);
          });
          String message = "Sorry, you already have such card!";
          Assertions.assertEquals(message, cre.getMessage());
        Assertions.assertEquals(1, user.getBankCards().size());

        Mockito.when(bankCardGenerator.generateBankCardNumber()).thenReturn("12435");

        BankCard bankCardWithTheSameCurrency = BankCard.builder()
                .cardCurrency(CardCurrency.EUR)
                .pinCode("1234")
                .cardNumber(bankCardGenerator.generateBankCardNumber())
                .build();

           cre =  Assertions.assertThrows(CardReleaseException.class, () -> {
            bankService.cardRelease(bankCardWithTheSameCurrency);
        });
        Assertions.assertEquals(1, user.getBankCards().size());

    }


}