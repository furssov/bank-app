package com.example.usersservice.feigns;

import com.springboot.conversion.beans.CurrencyConversionBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "conversion-service", url = "localhost:8100")
public interface TransferMoneyProxyService {

    @GetMapping("/currency-converter/from/{from}/to/{to}/quantity/{quantity}")
    CurrencyConversionBean getResultOfConversion(@PathVariable String from, @PathVariable String to, @PathVariable BigDecimal quantity);
}
