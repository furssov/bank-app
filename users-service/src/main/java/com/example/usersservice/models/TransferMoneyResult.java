package com.example.usersservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TransferMoneyResult {
    private String bankCardFrom;
    private String fromFirstName;
    private String fromSecondName;
    private String bankCardTo;
    private String toFirstName;
    private String toSecondName;
    private BigDecimal amount;
    private String currency;
}
