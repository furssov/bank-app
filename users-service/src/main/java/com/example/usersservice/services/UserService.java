package com.example.usersservice.services;

import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.models.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User save(User user);

    boolean deleteById(String id);

    User update(User user);

    Optional<User> findById(String id) throws UserException;

    List<User> findAll();

    void transferMoney(String fromId, String toId, BigDecimal amount) throws TransferMoneyException, UserException;

}
