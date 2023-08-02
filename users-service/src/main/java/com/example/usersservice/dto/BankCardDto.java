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
public class BankCardDto {
    @JsonProperty("pinCode")
    @NotBlank(message = "Please, enter your pin code")
    @Size(min = 4, max = 4, message = "Pin code wrong format")
    private String pinCode;

    @JsonProperty(value = "cardCurrency", required = true)
    @NotNull(message = "currency must not be null")
    private CardCurrency cardCurrency;
}
