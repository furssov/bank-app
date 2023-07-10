package com.example.usersservice.services.impl;

import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.feign.TransferMoneyServiceProxy;
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

    @Autowired
    public UserServiceImpl(TransferMoneyServiceProxy transferMoneyServiceProxy, UserRepository repository) {
        this.transferMoneyServiceProxy = transferMoneyServiceProxy;
        this.repository = repository;
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
    public User update(User user) {
        return repository.save(user);
    }

    @Override
    public Optional<User> findById(String id) throws UserException {
        Optional<User> user = repository.findById(id);
        if (user.isPresent()) {
            return user;
        }
        else {
            throw new UserException("No such user", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }

    private boolean validateAmount(BigDecimal money, BigDecimal amount) {
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
            Optional<User> toUserOpt = findById(toId);
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
