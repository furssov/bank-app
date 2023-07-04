package com.example.usersservice.dto;

import com.example.usersservice.models.CardCurrency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @NotBlank
    @Size(min = 2, max = 30, message = "first name minimum size is 2, max is 30")
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 30, message = "second name minimum size is 2, max is 30")
    private String secondName;

    @NotBlank
    @Size(min = 2, max = 30, message = "username min size is 2, max is 30")
    private String username;

    @NotBlank
    @Size(min = 5, max = 30, message = "password min size is 5, max is 30")
    private String password;

    private CardCurrency cardCurrency;
}
