package com.example.usersservice.models;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;

    @Field
    @NotBlank
    @Size(min = 2, max = 30, message = "first name minimum size is 2, max is 30")
    private String firstName;

    @Field
    @NotBlank
    @Size(min = 2, max = 30, message = "second name minimum size is 2, max is 30")
    private String secondName;

    @Field
    @NotBlank
    @Size(min = 2, max = 30, message = "username min size is 2, max is 30")
    private String username;

    @Field
    @NotBlank
    @Size(min = 5, max = 30, message = "password min size is 5, max is 30")
    private String password;

    @Field
    @Min(value = 0, message = "amount cant be less then null")
    private BigDecimal amount;

    @Field
    private CardCurrency cardCurrency;

    @Field
    private Role role;
}
