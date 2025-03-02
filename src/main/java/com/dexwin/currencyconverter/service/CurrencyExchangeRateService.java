package com.dexwin.currencyconverter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.IOException;
import java.util.Map;

@Service
public class CurrencyExchangeRateService implements CurrencyService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private static final String API_BASE_URL = "https://api.exchangerate.host";

    public CurrencyExchangeRateService() {
        this.webClient = WebClient.create(API_BASE_URL);
        this.objectMapper = new ObjectMapper();
    }

    // Constructor for testing purposes
    public CurrencyExchangeRateService(WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public double convert(String source, String target, double amount) {
        try {
            String ratesJson = fetchExchangeRates(source);
            Map<String, Double> rates = parseRates(ratesJson);

            if (source.equals(target)) {
                return amount;
            }

            Double targetRate = rates.get(target);
            if (targetRate == null) {
                throw new IllegalArgumentException("Unsupported target currency: " + target);
            }

            return amount * targetRate;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert currency: " + e.getMessage(), e);
        }
    }

    private String fetchExchangeRates(String baseCurrency) {
        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/latest")
                        .queryParam("base", baseCurrency)
                        .queryParam("symbols", "AUD,CAD,CHF,CNY,GBP,JPY,USD,EUR")
                        .queryParam("access_key", "7fbb9c6470ef56af1442d8c72e6491fa")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("API Response: " + response);
        return response;
    }

    private Map<String, Double> parseRates(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        if (!root.path("success").asBoolean()) {
            throw new IOException("API request failed");
        }

        JsonNode ratesNode = root.path("rates");
        return objectMapper.convertValue(ratesNode,
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Double.class));
    }
}