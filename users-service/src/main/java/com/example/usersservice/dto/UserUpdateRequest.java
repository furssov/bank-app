package com.example.usersservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateRequest extends UserRequest{
    @JsonProperty(value = "secureCode")
    @NotBlank(message = "Secure code can not be blank")
    private String code;
}
