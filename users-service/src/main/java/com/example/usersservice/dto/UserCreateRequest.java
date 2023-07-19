package com.example.usersservice.dto;

import com.example.usersservice.models.CardCurrency;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest extends UserRequest{

    @JsonProperty(value = "firstName", required = true)
    @NotBlank(message = "firstName must not be blank")
    @Size(min = 2, max = 30, message = "first name minimum size is 2, max is 30")
    private String firstName;

    @JsonProperty(value = "secondName", required = true)
    @NotBlank(message = "secondName must not be blank")
    @Size(min = 2, max = 30, message = "second name minimum size is 2, max is 30")
    private String secondName;

}
