package com.example.usersservice.controllers;

import com.example.usersservice.controllers.handlers.BankExceptionHandler;
import com.example.usersservice.dto.BankCardDto;
import com.example.usersservice.exceptions.ext.CardReleaseException;
import com.example.usersservice.exceptions.ext.TransferMoneyException;
import com.example.usersservice.feigns.TransferMoneyProxyService;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.CardCurrency;
import com.example.usersservice.models.User;
import com.example.usersservice.repos.BankRepository;
import com.example.usersservice.repos.UserRepository;
import com.example.usersservice.services.BankService;
import com.example.usersservice.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Autowired
    private BankRepository bankRepository;

    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockBean
    private Authentication authentication;

    @MockBean
    private TransferMoneyProxyService transferMoneyProxyService;

    @MockBean
    private UserRepository userRepository;

    @Value("${mongo.name.users}")
    private String usersCollection;

    @Value("${mongo.name.cards}")
    private String cardsCollection;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bankController).setControllerAdvice(BankExceptionHandler.class).build();
        mongoTemplate.remove(new Query(), cardsCollection);
        mongoTemplate.remove(new Query(), usersCollection);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

   @Test
    void CreateBankCard_AuthenticatedReleasesCard_StatusOk() throws Exception {
        String email = "fursov@gmail.com";
        String password = "12345";

       User user = new User.UserBuilder()
               .withUsername(email)
               .withPassword(password)
               .build();
       userService.save(user);

       Mockito.when(authentication.getName()).thenReturn(user.getId());

       BankCardDto bankCardDto = new BankCardDto();
       bankCardDto.setPinCode("1234");
       bankCardDto.setCardCurrency(CardCurrency.UAH);

       mockMvc.perform(post("/operations/bank-card")
               .contentType(MediaType.APPLICATION_JSON)
               .content(new ObjectMapper().writeValueAsString(bankCardDto)))
               .andExpect(status().isOk());
   }

   @Test
    void CardRelease_NotValidPin_StatusBadRequest() throws Exception {
       BankCardDto bankCardDto = new BankCardDto();
       bankCardDto.setPinCode("123");
       bankCardDto.setCardCurrency(CardCurrency.UAH);

       String message = "Pin code wrong format";

       mockMvc.perform(post("/operations/bank-card")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(new ObjectMapper().writeValueAsString(bankCardDto)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(message));
   }

   @Test
    void CardRelease_NullCurrency_StatusBadRequest() throws Exception {
       BankCardDto bankCardDto = new BankCardDto();
       bankCardDto.setPinCode("1234");
       String message = "currency must not be null";

       mockMvc.perform(post("/operations/bank-card")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(new ObjectMapper().writeValueAsString(bankCardDto)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(message));
   }

   @Test
    void CardRelease_ExistedCardCurrency_CardReleaseException() throws Exception {
       BankCardDto bankCardDto = new BankCardDto();
       bankCardDto.setPinCode("1234");
       bankCardDto.setCardCurrency(CardCurrency.UAH);

       User user = new User();
       userService.save(user);
       Mockito.when(authentication.getName()).thenReturn(user.getId());
       BankCard bankCard = new BankCard();
       bankCard.setPinCode("2222");
       bankCard.setCardCurrency(CardCurrency.UAH);
       bankService.cardRelease(bankCard);

       mockMvc.perform(post("/operations/bank-card")
               .contentType(MediaType.APPLICATION_JSON)
               .content(new ObjectMapper().writeValueAsString(bankCardDto)))
               .andExpect(status().isBadRequest())
               .andExpect(result ->
                       Assertions.assertTrue(result.getResolvedException() instanceof CardReleaseException));
   }

   @Test
    void TransferMoney_FromEURToUSD_StatusOk() {

   }
}