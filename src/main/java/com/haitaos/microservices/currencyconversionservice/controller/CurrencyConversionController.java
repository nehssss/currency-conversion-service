package com.haitaos.microservices.currencyconversionservice.controller;

import com.haitaos.microservices.currencyconversionservice.entity.CurrencyConversion;
import com.haitaos.microservices.currencyconversionservice.proxy.CurrencyExchangeProxy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@RestController
public class CurrencyConversionController {

    private CurrencyExchangeProxy currencyExchangeProxy;

    public CurrencyConversionController(CurrencyExchangeProxy currencyExchangeProxy) {
        this.currencyExchangeProxy = currencyExchangeProxy;
    }

    @GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversion(
            @PathVariable("from") String from,
            @PathVariable("to") String to,
            @PathVariable("quantity") BigDecimal quantity) {
        ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate()
                .getForEntity("http://localhost:8000/currency-exchange/from/{from}/to/{to}",
                        CurrencyConversion.class,
                        from,
                        to);

        CurrencyConversion responseEntityBody = responseEntity.getBody();
        CurrencyConversion currencyConversion = new CurrencyConversion(
                responseEntityBody.getId(),
                from,
                to,
                quantity);

        currencyConversion.setConversionMultiple(responseEntityBody
                .getConversionMultiple());

        currencyConversion.setTotalCalculatedAmount(quantity
                .multiply(responseEntityBody.getConversionMultiple()));

        currencyConversion.setEnvironment(responseEntityBody
                .getEnvironment());
        return currencyConversion;
    }

    @GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversionFeign(
            @PathVariable("from") String from,
            @PathVariable("to") String to,
            @PathVariable("quantity") BigDecimal quantity) {

        CurrencyConversion currencyConversion = this.currencyExchangeProxy
                .retrieveExchangeValue(from, to);

        currencyConversion.setQuantity(quantity);

        currencyConversion.setTotalCalculatedAmount(quantity
                .multiply(currencyConversion.getConversionMultiple()));

        return currencyConversion;
    }
}
