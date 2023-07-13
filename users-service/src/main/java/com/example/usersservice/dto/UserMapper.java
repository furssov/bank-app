package com.example.usersservice.dto;

import com.example.usersservice.models.Role;
import com.example.usersservice.models.User;

import java.math.BigDecimal;

public class UserMapper {

    public static User mapForCreating(UserDTO dto) {
        return new User.UserBuilder()
                .withAmount(BigDecimal.ZERO)
                .withCardCurrency(dto.getCardCurrency())
                .withFirstName(dto.getFirstName())
                .withPassword(dto.getPassword())
                .withRole(Role.USER)
                .withUsername(dto.getUsername())
                .withSecondName(dto.getSecondName())
                .build();
    }

    public static User mapForUpdating(UpdateUserRequest dto) {
        return new User.UserBuilder()
                .withUsername(dto.getUsername())
                .withPassword(dto.getPassword())
                .build();
    }




}
