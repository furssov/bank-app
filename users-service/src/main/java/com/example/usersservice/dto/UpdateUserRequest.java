package com.example.usersservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

//TODO
// create inheritance hierarchy of UserDTO
@Data
public class UpdateUserRequest {
    @JsonProperty("username")
    @NotBlank(message = "Username must not be blank")
    @Size(min = 2, max = 30, message = "username mi9n size is 2, max is 30")
    private String username;

    @JsonProperty("password")
    @NotBlank(message = "Password can not be blank")
    @Size(min = 5, max = 30, message = "password min size is 5, max is 30")
    private String password;

    @JsonProperty(value = "secureCode")
    @NotBlank(message = "Secure code can not be blank")
    private String code;
}
