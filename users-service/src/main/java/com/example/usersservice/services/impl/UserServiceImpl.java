package com.example.usersservice.services.impl;

import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.feign.TransferMoneyServiceProxy;
import com.example.usersservice.models.User;
import com.example.usersservice.repos.UserRepository;
import com.example.usersservice.services.UserService;
import com.springboot.conversion.beans.CurrencyConversionBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final TransferMoneyServiceProxy transferMoneyServiceProxy;
    private final UserRepository repository;

    @Autowired
    public UserServiceImpl(TransferMoneyServiceProxy transferMoneyServiceProxy, UserRepository repository) {
        this.transferMoneyServiceProxy = transferMoneyServiceProxy;
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        return repository.save(user);
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

    //TODO
    //Execute this method to bank service
    @Override
    @Transactional
    public void transferMoney(String fromId, String toId, BigDecimal amount) throws TransferMoneyException, UserException {
        Optional<User> fromUserOpt = findById(fromId);
        Optional<User> toUserOpt = findById(toId);
        if (fromUserOpt.isPresent() && toUserOpt.isPresent()) {
            User fromUser = fromUserOpt.get();
            User toUser = toUserOpt.get();
            CurrencyConversionBean conversion = transferMoneyServiceProxy
                    .getResultOfConversion(fromUser.getCardCurrency().name(), toUser.getCardCurrency().name(), amount);
            if (conversion != null) {
                if (fromUser.getAmount().subtract(amount).compareTo(BigDecimal.ZERO) >= 0) {
                    fromUser.setAmount(fromUser.getAmount().subtract(amount));
                    toUser.setAmount(toUser.getAmount().add(conversion.getTotalAmount()));
                    repository.save(fromUser);
                    repository.save(toUser);
                }
            }
        }
        else {
            throw new TransferMoneyException("Error while sending money", HttpStatus.BAD_REQUEST);
        }
    }
}
