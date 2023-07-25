package com.example.usersservice.dto;

import com.example.usersservice.models.CardCurrency;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BankCardDto {
    @JsonProperty("pinCode")
    @NotBlank(message = "Please, enter your pin code")
    @Size(min = 4, max = 4, message = "Pin code wrong format")
    private String pinCode;

    @JsonProperty("cardCurrency")
    private CardCurrency cardCurrency;
}
