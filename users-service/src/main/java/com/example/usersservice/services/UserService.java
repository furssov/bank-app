package com.example.usersservice.services;

import com.example.usersservice.exceptions.ext.UserException;
import com.example.usersservice.models.User;


public interface UserService {
    User save(User user) throws UserException;

    boolean deleteById(String id, String secureCode) throws UserException;

    User getByLogin(String login) throws UserException;

    User update(User user, String secureCode) throws UserException;

    User findById(String id) throws UserException;


}
