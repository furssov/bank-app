package com.example.usersservice.services;

import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.models.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User save(User user) throws UserException;

    boolean deleteById(String id);

    User getByLogin(String login) throws UserException;

    User update(User user);

    Optional<User> findById(String id) throws UserException;

    List<User> findAll();

    UserDetails loadUserByUsername(String username);
    void transferMoney(String fromId, String toId, BigDecimal amount) throws TransferMoneyException, UserException;

}
