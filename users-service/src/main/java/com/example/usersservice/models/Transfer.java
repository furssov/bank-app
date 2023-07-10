package com.example.usersservice.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transfer {
    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("toId")
    private String toId;
}
