package com.example.usersservice.dto;

import com.example.usersservice.models.Role;
import com.example.usersservice.models.User;

import java.math.BigDecimal;

public class UserMapper {

    public static User map(UserDTO dto) {
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setSecondName(dto.getSecondName());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRole(Role.USER);
        user.setAmount(BigDecimal.valueOf(0));
        user.setCardCurrency(dto.getCardCurrency());
        return user;
    }


}
