package com.springboot.conversion.controllers;

import com.springboot.conversion.beans.CurrencyConversionBean;
import com.springboot.conversion.feigns.CurrencyExchangeServiceProxy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.swing.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConversionController.class)
@ExtendWith(SpringExtension.class)
class ConversionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyExchangeServiceProxy exchangeServiceProxy;

    @Test
    void converter() throws Exception {
        String eur = "EUR";
        String usd = "USD";
        String uah  = "UAH";
        BigDecimal quantity = BigDecimal.valueOf(250);
        BigDecimal conversionEurUsd = BigDecimal.valueOf(0.92);
        BigDecimal conversionEurUah = BigDecimal.valueOf(40);

        CurrencyConversionBean currencyConversionBeanEurUsd = new CurrencyConversionBean();
        currencyConversionBeanEurUsd.setFrom(eur);
        currencyConversionBeanEurUsd.setTo(usd);
        currencyConversionBeanEurUsd.setConversionMultiple(conversionEurUsd);
        currencyConversionBeanEurUsd.setQuantity(quantity);
        currencyConversionBeanEurUsd.setTotalAmount(quantity.multiply(conversionEurUsd));
        Mockito.when(exchangeServiceProxy.retrieveExchangeValue(eur, usd)).thenReturn(currencyConversionBeanEurUsd);

        CurrencyConversionBean currencyConversionBeanEurUah = new CurrencyConversionBean();
        currencyConversionBeanEurUah.setFrom(eur);
        currencyConversionBeanEurUah.setTo(uah);
        currencyConversionBeanEurUah.setConversionMultiple(conversionEurUah);
        currencyConversionBeanEurUah.setQuantity(quantity);
        currencyConversionBeanEurUah.setTotalAmount(quantity.multiply(conversionEurUah));
        Mockito.when(exchangeServiceProxy.retrieveExchangeValue(eur, uah)).thenReturn(currencyConversionBeanEurUah);

        String requestEurUsd = "/currency-converter/from/"+ eur +"/to/"+ usd +"/quantity/" + quantity;
        String requestEurUah = "/currency-converter/from/"+ eur +"/to/"+ uah +"/quantity/" + quantity;

        mockMvc.perform(get(requestEurUsd))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value("230.0"))
                .andExpect(jsonPath("$.conversionMultiple").value(currencyConversionBeanEurUsd.getConversionMultiple()))
                .andExpect(jsonPath("$.from").value(currencyConversionBeanEurUsd.getFrom()))
                .andExpect(jsonPath("$.to").value(currencyConversionBeanEurUsd.getTo()));

        mockMvc.perform(get(requestEurUah))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(currencyConversionBeanEurUah.getTotalAmount()))
                .andExpect(jsonPath("$.conversionMultiple").value(currencyConversionBeanEurUah.getConversionMultiple()))
                .andExpect(jsonPath("$.from").value(currencyConversionBeanEurUah.getFrom()))
                .andExpect(jsonPath("$.to").value(currencyConversionBeanEurUah.getTo()));

    }

}