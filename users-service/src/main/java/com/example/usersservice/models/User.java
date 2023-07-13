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
    private String firstName;

    @Field
    @NotBlank
    private String secondName;

    @Field
    @NotBlank
    @Size(min = 2, max = 30, message = "username min size is 2, max is 30")
    @Email(message = "username must be an email!")
    private String username;

    @Field
    @NotBlank
    @Size(min = 5, max = 30, message = "password min size is 5, max is 30")
    @NotNull
    private String password;

    @Field
    @NotNull
    @Min(value = 0, message = "amount cant be less then null")
    private BigDecimal amount;

    @Field
    @NotNull
    private CardCurrency cardCurrency;

    @Field
    @NotNull
    private Role role;

    public static class UserBuilder {
        private User user;

        public UserBuilder() {
            user = new User();
        }

        public UserBuilder withId(String id) {
            user.setId(id);
            return this;
        }
        public UserBuilder withFirstName(String firstName) {
            user.setFirstName(firstName);
            return this;
        }

        public UserBuilder withSecondName(String secondName) {
            user.setSecondName(secondName);
            return this;
        }

        public UserBuilder withUsername(String username) {
            user.setUsername(username);
            return this;
        }

        public UserBuilder withPassword(String password) {
            user.setPassword(password);
            return this;
        }

        public UserBuilder withAmount(BigDecimal amount) {
            user.setAmount(amount);
            return this;
        }

        public UserBuilder withCardCurrency(CardCurrency cardCurrency) {
            user.setCardCurrency(cardCurrency);
            return this;
        }

        public UserBuilder withRole(Role role) {
            user.setRole(role);
            return this;
        }

        public User build() {
            return user;
        }
    }
}
