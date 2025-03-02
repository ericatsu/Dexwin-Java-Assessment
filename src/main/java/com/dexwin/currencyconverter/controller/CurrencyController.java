package com.dexwin.currencyconverter.controller;

import com.dexwin.currencyconverter.service.CurrencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/currencies")
public class CurrencyController {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyController.class);

    private final CurrencyService currencyService;

    @Autowired
    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/convert")
    public String convertCurrency(
            @RequestParam("source") String sourceCurrency,
            @RequestParam("target") String targetCurrency,
            @RequestParam("amount") double amount) {
        try {
            logger.info("Converting {} {} to {}", amount, sourceCurrency, targetCurrency);
            double convertedAmount = currencyService.convert(sourceCurrency, targetCurrency, amount);
            logger.info("Converted amount: {}", convertedAmount);
            return String.valueOf(convertedAmount);
        } catch (Exception e) {
            logger.error("Error converting currency", e);
            throw e;
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        logger.error("Error in currency conversion", e);
        return ResponseEntity.status(500).body("Error converting currency: " + e.getMessage());
    }
}