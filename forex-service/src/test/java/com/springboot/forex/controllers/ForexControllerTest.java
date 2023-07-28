package com.springboot.forex.controllers;

import com.springboot.forex.models.ExchangeValue;
import com.springboot.forex.repos.ExchangeValueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import javax.swing.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ForexController.class)
@ExtendWith(SpringExtension.class)
class ForexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeValueRepository exchangeValueRepository;


    @Test
    void testController() throws Exception {
        String eur = "EUR";
        String usd = "USD";
        String requestEurUsd = "/currency-exchange/from/EUR/to/USD";
        ExchangeValue exchangeValue = new ExchangeValue();
        exchangeValue.setConversionMultiple(BigDecimal.valueOf(0.92));
        Mockito.when(exchangeValueRepository.findByFromAndTo(eur, usd)).thenReturn(exchangeValue);
        mockMvc.perform(get(requestEurUsd))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversionMultiple").value("0.92"));

        Mockito.when(exchangeValueRepository.findByFromAndTo(eur, usd)).thenReturn(null);
        mockMvc.perform(get(requestEurUsd))
                .andExpect(status().isNotFound());
    }

}