package com.example.usersservice.mappers.impl;

import com.example.usersservice.dto.UserCreateRequest;
import com.example.usersservice.dto.UserRequest;
import com.example.usersservice.dto.UserUpdateRequest;
import com.example.usersservice.mappers.BankMapper;
import com.example.usersservice.models.Role;
import com.example.usersservice.models.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

//polymorphic ad hoc functions
@Component("userMapper")
public class UserMapper implements BankMapper<User, UserRequest> {

    public User map(UserRequest dto) {
        if (dto instanceof UserCreateRequest) {
            UserCreateRequest userCreateRequest = (UserCreateRequest) dto;
            return new User.UserBuilder()
                    .withBankCards(new ArrayList<>())
                    .withFirstName(userCreateRequest.getFirstName())
                    .withPassword(userCreateRequest.getPassword())
                    .withRole(Role.USER)
                    .withUsername(userCreateRequest.getUsername())
                    .withSecondName(userCreateRequest.getSecondName())
                    .build();
        }
        else {
            return map((UserUpdateRequest) dto);
        }
    }

    private User map(UserUpdateRequest dto) {
        return new User.UserBuilder()
                .withUsername(dto.getUsername())
                .withPassword(dto.getPassword())
                .build();
    }


}
