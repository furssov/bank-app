package com.example.usersservice.services.impl;

import com.example.usersservice.dto.SecureCodeResponse;
import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.feigns.SecureCodeProxyService;
import com.example.usersservice.feigns.TransferMoneyServiceProxy;
import com.example.usersservice.models.TransferMoneyResult;
import com.example.usersservice.models.User;
import com.example.usersservice.repos.UserRepository;
import com.example.usersservice.services.UserService;
import com.springboot.conversion.beans.CurrencyConversionBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    private final TransferMoneyServiceProxy transferMoneyServiceProxy;
    private final UserRepository repository;

    private final SecureCodeProxyService codeProxyService;

    @Autowired
    public UserServiceImpl(TransferMoneyServiceProxy transferMoneyServiceProxy, UserRepository repository, SecureCodeProxyService codeProxyService) {
        this.transferMoneyServiceProxy = transferMoneyServiceProxy;
        this.repository = repository;
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
    public boolean deleteById(String id) {
        Optional<User> user = repository.findById(id);
        if (user.isPresent()) {
            repository.deleteById(id);
            return true;
        }
        return false;

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

    @Override
    public List<User> findAll() {
        return repository.findAll();
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
    public TransferMoneyResult transferMoney(String toId, BigDecimal amount) throws TransferMoneyException, UserException {
        Optional<User> fromUserOpt = repository.findUserByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (fromUserOpt.isPresent()) {
            Optional<User> toUserOpt = repository.findById(toId);
            if (toUserOpt.isPresent()) {
                User fromUser = fromUserOpt.get();
                User toUser = toUserOpt.get();
                CurrencyConversionBean conversion = transferMoneyServiceProxy
                        .getResultOfConversion(fromUser.getCardCurrency().name(), toUser.getCardCurrency().name(), amount);
                if (conversion != null) {
                    if (validateAmount(fromUser.getAmount(), amount)) {
                        fromUser.setAmount(fromUser.getAmount().subtract(amount));
                        toUser.setAmount(toUser.getAmount().add(conversion.getTotalAmount()));
                        repository.save(fromUser);
                        repository.save(toUser);
                        return new TransferMoneyResult(fromUser.getId(), fromUser.getFirstName(), fromUser.getSecondName(),
                                toUser.getId(), toUser.getFirstName(), toUser.getSecondName(), amount, fromUser.getCardCurrency().name());
                    } else throw new TransferMoneyException("Transferring money exception", HttpStatus.BAD_REQUEST);
                } else
                    throw new TransferMoneyException("Such conversion is not available right now", HttpStatus.BAD_REQUEST);
            } else {
                throw new UserException("No user with such id", HttpStatus.BAD_REQUEST);
            }
        }
        return new TransferMoneyResult();
    }


}
