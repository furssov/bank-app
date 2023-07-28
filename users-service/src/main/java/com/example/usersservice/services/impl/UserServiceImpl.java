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
    private UserRepository repository;
    @Autowired
    private BankRepository bankRepository;
    @Autowired
    private SecureCodeProxyService codeProxyService;

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
}
