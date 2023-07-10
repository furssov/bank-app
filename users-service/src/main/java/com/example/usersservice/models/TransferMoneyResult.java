package com.example.usersservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferMoneyResult {
    private String bankIdFrom;
    private String fromFirstName;
    private String fromSecondName;
    private String bankIdTo;
    private String toFirstName;
    private String toSecondName;
    private BigDecimal amount;
    private String currency;
}
