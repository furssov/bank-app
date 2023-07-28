package com.example.usersservice.models;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
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
