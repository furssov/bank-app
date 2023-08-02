package com.example.usersservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;


@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class UserRequest {
    @JsonProperty(value = "username", required = true)
    @NotBlank(message = "Username must not be blank")
    @Size(min = 2, max = 30, message = "username min size is 2, max is 30")
    @Email(message = "Username must be an email")
    private String username;

    @JsonProperty(value = "password", required = true)
    @NotBlank(message = "Password must not be blank")
    @Size(min = 5, max = 30, message = "password min size is 5, max is 30")
    private String password;


}
