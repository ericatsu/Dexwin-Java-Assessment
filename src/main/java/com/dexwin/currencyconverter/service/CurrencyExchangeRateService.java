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
    private static final String API_KEY = "67ea6df2da6f91b3e6c5d455c0fdec7a";

    public CurrencyExchangeRateService() {
        this.webClient = WebClient.create(API_BASE_URL);
        this.objectMapper = new ObjectMapper();
    }

    public CurrencyExchangeRateService(WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public double convert(String source, String target, double amount) {
        try {
            if (source.equals(target)) {
                return amount;
            }

            String ratesJson = fetchExchangeRates();
            Map<String, Double> rates = parseRates(ratesJson);

            if (source.equals("USD")) {
                String targetKey = "USD" + target;
                Double targetRate = rates.get(targetKey);

                if (targetRate == null) {
                    throw new IllegalArgumentException("Unsupported target currency: " + target);
                }

                return amount * targetRate;
            }

            if (target.equals("USD")) {
                String sourceKey = "USD" + source;
                Double sourceRate = rates.get(sourceKey);

                if (sourceRate == null) {
                    throw new IllegalArgumentException("Unsupported source currency: " + source);
                }

                return amount / sourceRate;
            }

            String sourceKey = "USD" + source;
            String targetKey = "USD" + target;

            Double sourceRate = rates.get(sourceKey);
            Double targetRate = rates.get(targetKey);

            if (sourceRate == null) {
                throw new IllegalArgumentException("Unsupported source currency: " + source);
            }

            if (targetRate == null) {
                throw new IllegalArgumentException("Unsupported target currency: " + target);
            }

            double amountInUSD = amount / sourceRate;
            return amountInUSD * targetRate;

        } catch (Exception e) {
            throw new RuntimeException("Failed to convert currency: " + e.getMessage(), e);
        }
    }

    private String fetchExchangeRates() {
        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/live")
                        .queryParam("access_key", API_KEY)
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

        JsonNode quotesNode = root.path("quotes");
        if (quotesNode.isMissingNode()) {
            throw new IOException("Invalid API response format: 'quotes' not found");
        }

        Map<String, Double> rates = objectMapper.convertValue(quotesNode,
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Double.class));

        rates.put("USDUSD", 1.0);

        return rates;
    }
}