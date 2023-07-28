package com.example.usersservice.services;

import com.example.usersservice.dto.BankCardDto;
import com.example.usersservice.exceptions.TransferMoneyException;
import com.example.usersservice.exceptions.UserException;
import com.example.usersservice.models.BankCard;
import com.example.usersservice.models.TransferMoneyResult;
import com.example.usersservice.models.User;

import java.math.BigDecimal;


public interface UserService {
    User save(User user) throws UserException;

    boolean deleteById(String id, String secureCode) throws UserException;

    User getByLogin(String login) throws UserException;

    User update(User user, String secureCode) throws UserException;

    User findById(String id) throws UserException;


}
