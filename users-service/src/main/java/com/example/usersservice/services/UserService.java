package com.example.usersservice.services;

import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.models.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User save(User user);

    void deleteById(String id);

    User update(User user);

    Optional<User> findById(String id) throws UserException;

    List<User> findAll();

    void transferMoney(String fromId, String toId);

}
