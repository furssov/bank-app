package com.example.usersservice.gen;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class BankCardGeneratorImpl implements BankCardGenerator {

    @Override
    public String generateBankCardNumber() {
        return generateRandomNumber(16);
    }

    @Override
    public String generateBankCardCvv() {
        return generateRandomNumber(3);
    }

    @Override
    public String generateBankCardPinCode() {
        return generateRandomNumber(4);
    }

    private String generateRandomNumber(int digits) {
        Random random = new Random();
        StringBuilder stringBuilder  = new StringBuilder();
        int count = 0;
        while (count != digits) {
            stringBuilder.append(random.nextInt(10));
            count++;
        }
        return stringBuilder.toString();
    }
}
