package com.example.usersservice.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Document(collection = "bankCards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class BankCard {
    @Id
    private String id;

    @CreditCardNumber
    @Indexed(unique = true)
    private String cardNumber;

    @Field
    @Size(min = 3, max = 3, message = "Wrong cvv format")
    private String cvv;

    @Field
    @Size(min = 4, max = 4, message = "Wrong pin format")
    private String pinCode;

    @Field
    @NotBlank
    private CardCurrency cardCurrency;

    @Field
    @NotNull
    @Min(value = 0, message = "Amount can't be less then zero")
    private BigDecimal amount;


    @DBRef
    private User user;
}
