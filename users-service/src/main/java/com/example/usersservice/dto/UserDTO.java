package com.example.usersservice.dto;

import com.example.usersservice.models.CardCurrency;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @JsonProperty(value = "firstName", required = true)
    @NotBlank(message = "firstName must not be blank")
    @Size(min = 2, max = 30, message = "first name minimum size is 2, max is 30")
    private String firstName;

    @JsonProperty(value = "secondName", required = true)
    @NotBlank(message = "secondName must not be blank")
    @Size(min = 2, max = 30, message = "second name minimum size is 2, max is 30")
    private String secondName;

    @JsonProperty(value = "username", required = true)
    @NotBlank(message = "Username must not be blank")
    @Size(min = 2, max = 30, message = "username min size is 2, max is 30")
    @Email(message = "Username must be an email")
    private String username;

    @JsonProperty(value = "password", required = true)
    @NotBlank(message = "Password must not be blank")
    @Size(min = 5, max = 30, message = "password min size is 5, max is 30")
    private String password;

    @JsonProperty(value = "cardCurrency", required = true)
    private CardCurrency cardCurrency;


}
