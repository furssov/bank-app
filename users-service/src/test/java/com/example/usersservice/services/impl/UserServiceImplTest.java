package com.example.usersservice.services.impl;

import com.example.usersservice.dto.SecureCodeResponse;
import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.feigns.SecureCodeProxyService;
import com.example.usersservice.feigns.TransferMoneyProxyService;
import com.example.usersservice.gen.BankCardGenerator;
import com.example.usersservice.jwt.JwtAuthentication;
import com.example.usersservice.mappers.UserMapper;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.CardCurrency;
import com.example.usersservice.models.TransferMoneyResult;
import com.example.usersservice.models.User;
import com.example.usersservice.repos.BankRepository;
import com.example.usersservice.repos.UserRepository;
import com.example.usersservice.services.UserService;
import com.example.usersservice.validators.BankCardValidator;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {

    @TestConfiguration
    static class UserServiceTestBean {
        @Bean
        public UserService service() {
            return new UserServiceImpl();
        }
    }

    @Qualifier("service")
    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TransferMoneyProxyService transferMoneyProxyService;

    @MockBean
    private BankRepository bankRepository;

    @MockBean
    private SecureCodeProxyService secureCodeProxyService;

    @Autowired
    private BankCardGenerator bankCardGenerator;

    @Autowired
    private BankCardValidator bankCardValidator;

    @MockBean
    private Authentication authentication;

    @Test
    void save() throws UserException {
        User user = new User();
        String username = "furssov@gmail.com";
        String message = "User with such username has already exists";
        user.setUsername(username);
        Mockito.when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Assertions.assertEquals(user, userService.save(user));
        Mockito.when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
        UserException userException = Assertions.assertThrows(UserException.class, () -> {
           userService.save(user);
        });
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, userException.getHttpStatus());
        Assertions.assertEquals(message, userException.getMessage());
    }

    @Test
    void deleteById() throws UserException {
        String id = UUID.randomUUID().toString();
        String secureCode = UUID.randomUUID().toString();
        String username = "fursov@gmail.com";
        String message1 = "No such user";
        String message2 = "Wrong secure code";
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
        Mockito.when(secureCodeProxyService.getSecureCode(username)).thenReturn(new SecureCodeResponse(secureCode, username));
        Assertions.assertTrue(userService.deleteById(id, secureCode));

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.empty());

        UserException userException = Assertions.assertThrows(UserException.class, () -> {
           userService.deleteById(id, secureCode);
        });
        Assertions.assertEquals(message1, userException.getMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, userException.getHttpStatus());

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userException = Assertions.assertThrows(UserException.class, () -> {
            userService.deleteById(id, UUID.randomUUID().toString());
        });
        Assertions.assertEquals(message2, userException.getMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, userException.getHttpStatus());

    }

    @Test
    void findByUsername() throws UserException {
        String username = "fursovd70@gmail.com";
        String message = "Username was not found";
        User user = new User();
        user.setUsername(username);
        Mockito.when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
        Assertions.assertEquals(user, userService.getByLogin(username));

        Mockito.when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());
        UserException ue = Assertions.assertThrows(UserException.class, () -> {
           userService.getByLogin(username);
        });

        Assertions.assertEquals(message, ue.getMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ue.getHttpStatus());
    }


    @Test
    void update() throws UserException {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String oldUsername = "fursov@gmail.com";
        String oldPassword = "12345";
        String id = UUID.randomUUID().toString();
        String code = UUID.randomUUID().toString();
        User prevUser = new User();
        prevUser.setId(id);
        prevUser.setUsername(oldUsername);
        prevUser.setPassword(oldPassword);

        User updatedUser = new User();
        updatedUser.setId(id);
        updatedUser.setUsername("new@gmail.com");
        updatedUser.setPassword("a12345");
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(oldUsername);

        Mockito.when(secureCodeProxyService.getSecureCode(oldUsername)).thenReturn(new SecureCodeResponse(code, oldUsername));

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(prevUser));
        User userToDb = userService.findById(updatedUser.getId());

        Mockito.when(userRepository.save(userToDb)).thenReturn(userToDb);
        Assertions.assertEquals(updatedUser, userService.update(updatedUser, code));

        Mockito.when(secureCodeProxyService.getSecureCode(oldUsername)).thenReturn(new SecureCodeResponse(null, oldUsername));

        String message = "Wrong secure code";

        UserException ue = Assertions.assertThrows(UserException.class, () -> {
            userService.update(updatedUser, code);
        });

        Assertions.assertEquals(message, ue.getMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ue.getHttpStatus());
    }

    @Test
    void findById() throws UserException {
        String id = UUID.randomUUID().toString();
        String message = "No such user";
        User user = new User();
        user.setId(id);

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
        Assertions.assertEquals(user, userService.findById(id));

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.empty());

        UserException ue = Assertions.assertThrows(UserException.class, () -> {
           userService.findById(id);
        });

        Assertions.assertEquals(message, ue.getMessage());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, ue.getHttpStatus());
    }

    @Test
    void transferMoney() throws TransferMoneyException, UserException {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String fromCard = bankCardGenerator.generateBankCard(16);
        String toCard = bankCardGenerator.generateBankCard(16);
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

        Assertions.assertEquals(result, userService.transferMoney(fromCard, amount, toCard));

        Assertions.assertEquals(BigDecimal.valueOf(0), fromBankCard.getAmount());
        Assertions.assertEquals(BigDecimal.valueOf(300), toBankCard.getAmount());


    }
}