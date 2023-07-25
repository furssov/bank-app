package com.example.usersservice.services.impl;

import com.example.usersservice.dto.SecureCodeResponse;
import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.feigns.SecureCodeProxyService;
import com.example.usersservice.feigns.TransferMoneyProxyService;
import com.example.usersservice.gen.BankCardGenerator;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.TransferMoneyResult;
import com.example.usersservice.models.User;
import com.example.usersservice.repos.BankRepository;
import com.example.usersservice.repos.UserRepository;
import com.example.usersservice.services.UserService;
import com.example.usersservice.validators.BankCardValidator;
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

    @Autowired
    private TransferMoneyProxyService transferMoneyServiceProxy;
    @Autowired
    private UserRepository repository;
    @Autowired
    private BankRepository bankRepository;
    @Autowired
    private SecureCodeProxyService codeProxyService;
    @Autowired
    private BankCardGenerator bankCardGenerator;
    @Autowired
    private BankCardValidator bankCardValidator;


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
            User userDB = user.get();
            String userEmail = userDB.getUsername();
            SecureCodeResponse scr = codeProxyService.getSecureCode(userEmail);
            if (validateEmailAndCode(userEmail, scr.getReceiverEmail(), secureCode, scr.getSecureCode())) {
                codeProxyService.deleteSecureCode(userEmail);
                repository.deleteById(id);
                if (userDB.getBankCards() != null) {
                    userDB.getBankCards().forEach(bankCard -> bankRepository.deleteById(bankCard.getId()));
                }
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

    //TODO review
    @Override
    @Transactional
    public TransferMoneyResult transferMoney(String fromCard, BigDecimal amount, String toCard) throws TransferMoneyException, UserException {
      Optional<User> user = repository.findById(SecurityContextHolder.getContext().getAuthentication().getName());
      if (user.isPresent()) {
          User userFrom = user.get();
          //TODO extract to method
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
                      if (bankCardValidator.validateAmount(fromAmount, amount)) {
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
            String generatedBankCard = bankCardGenerator.generateBankCard(16);
            String generatedCvv = bankCardGenerator.generateBankCard(3);
            if (bankCardValidator.validateBankCard(
                    bankRepository.findAll(),
                    generatedBankCard,
                    bankCard.getCardCurrency(),
                    generatedCvv)) {

                bankCard.setCardNumber(generatedBankCard);
                bankCard.setCvv(generatedCvv);
                bankCard.setUser(user.get());

                bankRepository.save(bankCard);
                bankCards.add(bankCard);
                user.get().setBankCards(bankCards);
                repository.save(user.get());
                return true;
            }
            return false;
        }
        return false;
    }




}
