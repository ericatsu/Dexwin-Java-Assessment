package com.dexwin.currencyconverter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CurrencyExchangeRateServiceTest {

    private CurrencyExchangeRateService service;
    private WebClient webClient;
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        webClient = Mockito.mock(WebClient.class);
        requestHeadersUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        service = new CurrencyExchangeRateService(webClient);
    }

    @Test
    void shouldConvertEurToUsdSuccessfully() {
        String mockResponse = "{\"success\":true,\"base\":\"EUR\",\"rates\":{\"USD\":1.1}}";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockResponse));

        double result = service.convert("EUR", "USD", 100.0);
        assertEquals(110.0, result, 0.001);
    }

    @Test
    void shouldReturnSameAmountForSameCurrencies() {
        String mockResponse = "{\"success\":true,\"base\":\"EUR\",\"rates\":{}}";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockResponse));

        double result = service.convert("EUR", "EUR", 100.0);
        assertEquals(100.0, result, 0.001);
    }

    @Test
    void shouldThrowExceptionForInvalidTargetCurrency() {
        String mockResponse = "{\"success\":true,\"base\":\"EUR\",\"rates\":{\"USD\":1.1}}";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockResponse));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.convert("EUR", "GBP", 100.0);
        });

        assertTrue(exception.getMessage().contains("Unsupported target currency: GBP"));
    }

    @Test
    void shouldThrowExceptionForApiFailure() {
        String mockResponse = "{\"success\":false,\"error\":\"Invalid request\"}";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockResponse));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.convert("EUR", "USD", 100.0);
        });
        assertTrue(exception.getMessage().contains("Failed to convert currency"));
    }
}