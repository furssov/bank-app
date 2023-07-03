package com.springboot.conversion.controllers;

import com.springboot.conversion.beans.CurrencyConversionBean;
import com.springboot.conversion.feigns.CurrencyExchangeServiceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/currency-converter")
public class ConversionController {

    private final CurrencyExchangeServiceProxy proxy;

    @Autowired
    public ConversionController(CurrencyExchangeServiceProxy proxy) {
        this.proxy = proxy;
    }

    @GetMapping("/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversionBean convertCurrency(@PathVariable String from, @PathVariable String to, @PathVariable BigDecimal quantity){
        CurrencyConversionBean bean = proxy.retrieveExchangeValue(from, to);
        return new CurrencyConversionBean(bean.getId(), bean.getFrom(), bean.getTo(),
                bean.getConversionMultiple(), quantity,
                quantity.multiply(bean.getConversionMultiple()), bean.getPort());
    }
}
