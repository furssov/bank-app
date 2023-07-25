package com.example.usersservice.gen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class BankCardGeneratorImplTest {
    @Autowired
    private BankCardGenerator bankCardGenerator;

    @Test
    void generateBankCard() {
        List<String> bankCardsNumbers = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            bankCardsNumbers.add(bankCardGenerator.generateBankCard(16));
        }
        List<String> bcWithoutDuplicates = bankCardsNumbers.stream().distinct().collect(Collectors.toList());

        Assertions.assertEquals(bcWithoutDuplicates, bankCardsNumbers);
    }
}