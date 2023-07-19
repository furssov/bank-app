package com.example.usersservice.services.impl;

import com.example.usersservice.dto.SecureCodeResponse;
import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.feigns.SecureCodeProxyService;
import com.example.usersservice.feigns.TransferMoneyProxyService;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.TransferMoneyResult;
import com.example.usersservice.models.User;
import com.example.usersservice.repos.BankRepository;
import com.example.usersservice.repos.UserRepository;
import com.example.usersservice.services.UserService;
import com.springboot.conversion.beans.CurrencyConversionBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class UserServiceImpl implements UserService{

    private final TransferMoneyProxyService transferMoneyServiceProxy;
    private final UserRepository repository;
    private final BankRepository bankRepository;
    private final SecureCodeProxyService codeProxyService;

    @Autowired
    public UserServiceImpl(TransferMoneyProxyService transferMoneyServiceProxy, UserRepository repository, BankRepository bankRepository, SecureCodeProxyService codeProxyService) {
        this.transferMoneyServiceProxy = transferMoneyServiceProxy;
        this.repository = repository;
        this.bankRepository = bankRepository;
        this.codeProxyService = codeProxyService;
    }

    @Override
    @Transactional
    public User save(User user) throws UserException {
        if (repository.findUserByUsername(user.getUsername()).isEmpty()) {
            return repository.save(user);
        }
        else {
            throw new UserException("User with such username has already exists", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @Transactional
    public boolean deleteById(String id, String secureCode) throws UserException {
        Optional<User> user = repository.findById(id);
        if (user.isPresent()) {
            String userEmail = user.get().getUsername();
            SecureCodeResponse scr = codeProxyService.getSecureCode(userEmail);
            if (validateEmailAndCode(userEmail, scr.getReceiverEmail(), secureCode, scr.getSecureCode())) {
                codeProxyService.deleteSecureCode(userEmail);
                repository.deleteById(id);
                return true;
            }
            else throw new UserException("Wrong secure code", HttpStatus.BAD_REQUEST);
        }
        else throw new UserException("No such user", HttpStatus.BAD_REQUEST);

    }

    @Override
    public User getByLogin(String login) throws UserException {
       Optional<User> user = repository.findUserByUsername(login);
       if (user.isPresent()) {

           return user.get();
       }
       else {
           throw new UserException("Username was not found", HttpStatus.BAD_REQUEST);
       }
    }

    @Override
    @Transactional
    public User update(User user, String secureCode) throws UserException {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        SecureCodeResponse scr = codeProxyService.getSecureCode(userEmail);
        if (validateEmailAndCode(userEmail, scr.getReceiverEmail(), secureCode, scr.getSecureCode())) {
            codeProxyService.deleteSecureCode(userEmail);
            User userToDB = findById(user.getId());
            userToDB.setUsername(user.getUsername());
            userToDB.setPassword(user.getPassword());
            return repository.save(userToDB);
        }
        else {
            throw new UserException("Wrong secure code", HttpStatus.BAD_REQUEST);
        }
    }

    private static boolean validateEmailAndCode(String userEmail, String emailFromMailSender, String code, String codeFromMailSender) {
        if (userEmail != null && code != null) {
            return userEmail.equals(emailFromMailSender) && code.equals(codeFromMailSender);
        }
        return false;
    }

    @Override
    public User findById(String id) throws UserException {
        Optional<User> user = repository.findById(id);
        if (user.isPresent()) {
            return user.get();
        }
        else {
            throw new UserException("No such user", HttpStatus.NOT_FOUND);
        }
    }


    private static boolean validateAmount(BigDecimal money, BigDecimal amount) {
        if (money.subtract(amount).compareTo(BigDecimal.ZERO) >= 0 && amount.compareTo(BigDecimal.ZERO) > 0) {
            return true;
        }
        else {
            return false;
        }
    }
    @Override
    @Transactional
    public TransferMoneyResult transferMoney(String fromCard, BigDecimal amount, String toCard) throws TransferMoneyException, UserException {
      Optional<User> user = repository.findById(SecurityContextHolder.getContext().getAuthentication().getName());

      if (user.isPresent()) {
          User userFrom = user.get();
          Optional<BankCard> bankCardFromOpt = userFrom.getBankCards()
                  .stream()
                  .filter(bankCard -> bankCard.getCardNumber().equals(fromCard))
                  .findFirst();
          if (bankCardFromOpt.isPresent()) {
              BankCard bankCardFrom = bankCardFromOpt.get();
              Optional<BankCard> bankCardToOpt = bankRepository.findBankCardByCardNumber(toCard);
              if (bankCardToOpt.isPresent()) {
                  BankCard bankCardTo = bankCardToOpt.get();
                  CurrencyConversionBean ccb = transferMoneyServiceProxy.getResultOfConversion(bankCardFrom.getCardCurrency().name(),
                          bankCardTo.getCardCurrency().name(), amount);
                  synchronized (bankCardFrom) {
                      BigDecimal fromAmount = bankCardFrom.getAmount();
                      if (validateAmount(fromAmount, amount)) {
                          synchronized (bankCardTo) {
                              bankCardFrom.setAmount(fromAmount.subtract(amount));
                              bankRepository.save(bankCardFrom);
                              BigDecimal toAmount = bankCardTo.getAmount();
                              bankCardTo.setAmount(toAmount.add(ccb.getTotalAmount()));
                              bankRepository.save(bankCardTo);
                          }
                          return new TransferMoneyResult(bankCardFrom.getCardNumber(), userFrom.getFirstName(), userFrom.getSecondName(),
                                  bankCardTo.getCardNumber(), bankCardTo.getUser().getFirstName(), bankCardTo.getUser().getSecondName(), ccb.getTotalAmount(), bankCardTo.getCardCurrency().name());
                      }
                      else {
                          throw new TransferMoneyException("Not enough money on your card", HttpStatus.BAD_REQUEST);
                      }
                  }
              }
              else {
                  throw new TransferMoneyException("No such card", HttpStatus.BAD_REQUEST);
              }
          }
          else {
              throw new TransferMoneyException("You don't have such card", HttpStatus.BAD_REQUEST);
          }
      }
      else {
          throw new UserException("No such user", HttpStatus.UNAUTHORIZED);
      }
    }

    @Override
    @Transactional
    public boolean cardRelease(BankCard bankCard) {
        Optional<User> user = repository.findById(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user.isPresent()) {
            List<BankCard> bankCards = new ArrayList<>(user.get().getBankCards());
           //TODO generating/validating
            bankCard.setCardNumber(UUID.randomUUID().toString());
            bankCard.setUser(user.get());
            bankRepository.save(bankCard);
            bankCards.add(bankCard);
            user.get().setBankCards(bankCards);
            repository.save(user.get());
            return true;
        }
        return false;
    }


}
