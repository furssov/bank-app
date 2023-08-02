package com.example.usersservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest extends UserRequest{
    @JsonProperty(value = "secureCode")
    @NotBlank(message = "Secure code can not be blank")
    private String code;
}
