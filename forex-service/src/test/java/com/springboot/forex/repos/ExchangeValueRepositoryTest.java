package com.springboot.forex.repos;

import com.springboot.forex.models.ExchangeValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class ExchangeValueRepositoryTest {

    private final ExchangeValueRepository exchangeValueRepository;

    @Autowired
    ExchangeValueRepositoryTest(ExchangeValueRepository exchangeValueRepository) {
        this.exchangeValueRepository = exchangeValueRepository;
    }

    @Test
    void findByFromAndTo() {
        ExchangeValue exchangeValue = new ExchangeValue();
        String eur = "EUR";
        String usd = "USD";
        String uah = "UAH";
        exchangeValue.setFrom("EUR");
        exchangeValue.setTo("USD");
        exchangeValue.setConversionMultiple(BigDecimal.valueOf(0.92));
        exchangeValue.setId(6L);
        Assertions.assertEquals(exchangeValue, exchangeValueRepository.findByFromAndTo(eur, usd));
        Assertions.assertEquals("40.00", exchangeValueRepository.findByFromAndTo(eur, uah).getConversionMultiple().toString());
        Assertions.assertEquals("37.00", exchangeValueRepository.findByFromAndTo(usd, uah).getConversionMultiple().toString());
    }
}