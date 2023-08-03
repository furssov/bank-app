package com.example.usersservice.controllers;

import com.example.usersservice.controllers.handlers.BankExceptionHandler;
import com.example.usersservice.dto.BankCardDto;
import com.example.usersservice.exceptions.ext.BankCardException;
import com.example.usersservice.exceptions.ext.CardReleaseException;
import com.example.usersservice.exceptions.ext.TransferMoneyException;
import com.example.usersservice.feigns.TransferMoneyProxyService;
import com.example.usersservice.models.*;
import com.example.usersservice.services.BankService;
import com.example.usersservice.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.conversion.beans.CurrencyConversionBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
class BankControllerTest {

    @Autowired
    private BankController bankController;

    @Autowired
    @Qualifier("userServiceImpl")
    private UserService userService;

    @Qualifier("bankServiceImpl")
    @Autowired
    private BankService bankService;

    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockBean
    private Authentication authentication;

    @MockBean
    private TransferMoneyProxyService transferMoneyProxyService;

    @Value("${mongo.name.users}")
    private String usersCollection;

    @Value("${mongo.name.cards}")
    private String cardsCollection;

    private static final String PIN = "1234";

    private static final BigDecimal AMOUNT = BigDecimal.valueOf(150);

    private static final String EMAIL = "fursov@gmail.com";

    private static final String RECIPIENT_EMAIL = "fursova@gmail.com";

    private static final String PASSWORD = "12345";

    private static final String EUR = CardCurrency.EUR.name();

    private static final String USD = CardCurrency.USD.name();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bankController).setControllerAdvice(BankExceptionHandler.class).build();
        mongoTemplate.remove(new Query(), cardsCollection);
        mongoTemplate.remove(new Query(), usersCollection);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private User createUser(String email, String password) {
        return new User.UserBuilder()
                .withUsername(email)
                .withPassword(password)
                .build();
    }

    private BankCardDto createBankCardDto(CardCurrency cardCurrency) {
        BankCardDto bankCardDto = new BankCardDto();
        bankCardDto.setPinCode(PIN);
        bankCardDto.setCardCurrency(cardCurrency);
        return bankCardDto;
    }

    private BankCard createBankCard(CardCurrency cardCurrency, BigDecimal amount) {
        return BankCard.builder()
                .pinCode(PIN)
                .cardCurrency(cardCurrency)
                .amount(amount)
                .build();
    }

    private Transfer createTransfer(String bankCardFrom, String bankCardTo, BigDecimal amount) {
        Transfer transfer = new Transfer();
        transfer.setFromCard(bankCardFrom);
        transfer.setToCard(bankCardTo);
        transfer.setAmount(amount);
        return transfer;
    }

    private TransferMoneyResult createTransferMoneyResult(String cardFrom, String cardTo, BigDecimal amount, String currency) {
        TransferMoneyResult transferMoneyResult = new TransferMoneyResult();
        transferMoneyResult.setCurrency(currency);
        transferMoneyResult.setBankCardTo(cardTo);
        transferMoneyResult.setBankCardFrom(cardFrom);
        transferMoneyResult.setAmount(amount);
        return transferMoneyResult;
    }
    @Test
    void CreateBankCard_AuthenticatedReleasesCard_StatusOk() throws Exception {
       User user = createUser(EMAIL, PASSWORD);
       userService.save(user);
       Mockito.when(authentication.getName()).thenReturn(user.getId());

       BankCardDto bankCardDto = createBankCardDto(CardCurrency.UAH);

       mockMvc.perform(post("/operations/bank-card")
               .contentType(MediaType.APPLICATION_JSON)
               .content(new ObjectMapper().writeValueAsString(bankCardDto)))
               .andExpect(status().isOk());
   }

   @Test
    void CardRelease_NotValidPin_StatusBadRequest() throws Exception {
        String pin = "123";
       BankCardDto bankCardDto = createBankCardDto(CardCurrency.EUR);
       bankCardDto.setPinCode(pin);

       String message = "Pin code wrong format";

       mockMvc.perform(post("/operations/bank-card")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(new ObjectMapper().writeValueAsString(bankCardDto)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(message));
   }

   @Test
    void CardRelease_NullCurrency_StatusBadRequest() throws Exception {
       BankCardDto bankCardDto = createBankCardDto(null);

       String message = "currency must not be null";

       mockMvc.perform(post("/operations/bank-card")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(new ObjectMapper().writeValueAsString(bankCardDto)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(message));
   }

   @Test
    void CardRelease_ExistedCardCurrency_CardReleaseException() throws Exception {
       BankCardDto bankCardDto = createBankCardDto(CardCurrency.UAH);

       User user = new User();
       userService.save(user);

       Mockito.when(authentication.getName()).thenReturn(user.getId());

       BankCard bankCard = createBankCard(CardCurrency.UAH, AMOUNT);
       bankService.cardRelease(bankCard);

       mockMvc.perform(post("/operations/bank-card")
               .contentType(MediaType.APPLICATION_JSON)
               .content(new ObjectMapper().writeValueAsString(bankCardDto)))
               .andExpect(status().isBadRequest())
               .andExpect(result ->
                       Assertions.assertTrue(result.getResolvedException() instanceof CardReleaseException));
   }

   @Test
    void TransferMoney_FromEURToUSD_StatusOk() throws Exception {
        BigDecimal conversion = BigDecimal.valueOf(0.92);
        BigDecimal result = AMOUNT.multiply(conversion);

        User user = createUser(EMAIL, PASSWORD);
       userService.save(user);

       String id = user.getId();

       Mockito.when(authentication.getName()).thenReturn(id);
       BankCard bankCard = createBankCard(CardCurrency.EUR, AMOUNT);
       bankService.cardRelease(bankCard);

       User userTo = createUser(RECIPIENT_EMAIL, PASSWORD);
       userService.save(userTo);

       id = userTo.getId();

       Mockito.when(authentication.getName()).thenReturn(id);
       BankCard bankCardTo = createBankCard(CardCurrency.USD, AMOUNT);
       bankService.cardRelease(bankCardTo);

       id = user.getId();
       Mockito.when(authentication.getName()).thenReturn(id);


       Mockito.when(transferMoneyProxyService.getResultOfConversion(EUR, USD, BigDecimal.valueOf(150)))
               .thenReturn(new CurrencyConversionBean(1l, EUR, USD, conversion, AMOUNT, AMOUNT.multiply(conversion), 0));

       Transfer transfer = createTransfer(bankCard.getCardNumber(), bankCardTo.getCardNumber(), AMOUNT);

       TransferMoneyResult transferMoneyResult = createTransferMoneyResult(bankCard.getCardNumber(),
               bankCardTo.getCardNumber(), result, USD);

       mockMvc.perform(post("/operations/transfer")
               .contentType(MediaType.APPLICATION_JSON)
               .content(new ObjectMapper().writeValueAsString(transfer)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.amount").value("138.0"))
               .andExpect(jsonPath("$.currency").value(transferMoneyResult.getCurrency()))
               .andExpect(jsonPath("$.bankCardFrom").value(transferMoneyResult.getBankCardFrom()))
               .andExpect(jsonPath("$.bankCardTo").value(transferMoneyResult.getBankCardTo()));
   }

   @Test
    void TransferMoney_NotOwnerOfTheCard_BankCardException() throws Exception {
        String message = "You don't have this card";
       User user = createUser(EMAIL, PASSWORD);
       userService.save(user);

       String id = user.getId();

       Mockito.when(authentication.getName()).thenReturn(id);
       BankCard bankCard = createBankCard(CardCurrency.EUR, AMOUNT);

       User anotherUser = createUser(RECIPIENT_EMAIL, PASSWORD);
       userService.save(anotherUser);

       id = anotherUser.getId();
       Mockito.when(authentication.getName()).thenReturn(id);
        bankService.cardRelease(bankCard);

        id = user.getId();
       Mockito.when(authentication.getName()).thenReturn(id);
       Transfer transfer = createTransfer(bankCard.getCardNumber(), bankCard.getCardNumber(), AMOUNT);

       mockMvc.perform(post("/operations/transfer")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(new ObjectMapper().writeValueAsString(transfer)))
               .andExpect(status().isBadRequest())
               .andExpect(result ->
                       Assertions.assertTrue(result.getResolvedException() instanceof BankCardException))
               .andExpect(result ->
                       Assertions.assertTrue(result.getResolvedException().getMessage().equals(message)));
   }

   @Test
    void TransferMoney_CardDoesntExist_BankCardException() throws Exception {
        String message = "There is no any bank card by inputted card number";
       User user = createUser(EMAIL, PASSWORD);
       userService.save(user);

       String id = user.getId();

       Mockito.when(authentication.getName()).thenReturn(id);
       BankCard bankCard = createBankCard(CardCurrency.EUR, AMOUNT);

       bankService.cardRelease(bankCard);

       String toCard = UUID.randomUUID().toString();
       Transfer transfer = createTransfer(bankCard.getCardNumber(), toCard, AMOUNT);


       mockMvc.perform(post("/operations/transfer")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(new ObjectMapper().writeValueAsString(transfer)))
               .andExpect(status().isBadRequest())
               .andExpect(result ->
                       Assertions.assertTrue(result.getResolvedException() instanceof BankCardException))
               .andExpect(result ->
                       Assertions.assertTrue(result.getResolvedException().getMessage().equals(message)));
   }

   @Test
    void TransferMoney_NotEnoughMoney_TransferMoneyException() throws Exception {
        BigDecimal amount = AMOUNT.add(BigDecimal.valueOf(1));
        BigDecimal conversion = BigDecimal.valueOf(0.92);
        BigDecimal res = amount.multiply(conversion);
       User user = createUser(EMAIL, PASSWORD);
       userService.save(user);

       String id = user.getId();

       Mockito.when(authentication.getName()).thenReturn(id);
       BankCard bankCard = createBankCard(CardCurrency.EUR, AMOUNT);
       bankService.cardRelease(bankCard);

       User userTo = createUser(RECIPIENT_EMAIL, PASSWORD);
       userService.save(userTo);

       id = userTo.getId();

       Mockito.when(authentication.getName()).thenReturn(id);
       BankCard bankCardTo = createBankCard(CardCurrency.USD, AMOUNT);
       bankService.cardRelease(bankCardTo);

       id = user.getId();
       Mockito.when(authentication.getName()).thenReturn(id);

       Transfer transfer = createTransfer(bankCard.getCardNumber(), bankCardTo.getCardNumber(), amount);

       Mockito.when(transferMoneyProxyService.getResultOfConversion(EUR, USD, amount))
               .thenReturn(new CurrencyConversionBean(1l, EUR, USD, conversion, amount, res, 0));


       TransferMoneyResult transferMoneyResult =
               createTransferMoneyResult(bankCard.getCardNumber(), bankCardTo.getCardNumber(), res, USD);

       mockMvc.perform(post("/operations/transfer")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(new ObjectMapper().writeValueAsString(transfer)))
               .andExpect(status().isBadRequest())
               .andExpect(result ->
                       Assertions.assertTrue(result.getResolvedException() instanceof TransferMoneyException))
               .andExpect(result ->
                       Assertions.assertTrue(result.getResolvedException().getMessage().equals("Not enough money on your card")));
   }
}