package com.example.usersservice.mappers;

import com.example.usersservice.dto.BankCardDto;
import com.example.usersservice.models.BankCard;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("cardMapper")
public class CardMapper implements BankMapper<BankCard, BankCardDto> {

    @Override
    public BankCard map(BankCardDto dto) {
        return BankCard.builder()
                .cardCurrency(dto.getCardCurrency())
                .pinCode(dto.getPinCode())
                .amount(BigDecimal.ZERO)
                .build();
    }
}
