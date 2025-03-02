package com.dexwin.currencyconverter.config;

import com.dexwin.currencyconverter.service.CurrencyExchangeRateService;
import com.dexwin.currencyconverter.service.CurrencyService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestConfiguration
public class CurrencyConverterTestConfig {

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public WebClient webClient() {
        WebClient webClientMock = Mockito.mock(WebClient.class);

        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        AtomicReference<String> capturedUri = new AtomicReference<>();

        when(webClientMock.get())
                .thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(any(Function.class))).thenAnswer(invocation -> {
            Function<UriComponentsBuilder, URI> function = invocation.getArgument(0);
            UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host("api.exchangerate.host");
            URI uri = function.apply(builder);

            capturedUri.set(uri.toString());

            return requestHeadersSpec;
        });

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(String.class)).thenAnswer(invocation -> {
            String uriString = capturedUri.get();

            if (uriString != null && uriString.contains("base=USD")) {
                // USD to EUR rate (less than 1.0)
                return Mono.just(
                        "{\"success\":true,\"base\":\"USD\",\"rates\":{\"EUR\":0.85,\"AUD\":1.35,\"CAD\":1.25,\"CHF\":0.95,\"CNY\":6.45,\"GBP\":0.75,\"JPY\":110.25,\"USD\":1.0}}");
            } else if (uriString != null && uriString.contains("base=EUR")) {
                // EUR to USD rate (greater than 1.0)
                return Mono.just(
                        "{\"success\":true,\"base\":\"EUR\",\"rates\":{\"USD\":1.18,\"AUD\":1.59,\"CAD\":1.47,\"CHF\":1.12,\"CNY\":7.65,\"GBP\":0.88,\"JPY\":130.0,\"EUR\":1.0}}");
            } else {
                return Mono.just(
                        "{\"success\":true,\"base\":\"USD\",\"rates\":{\"EUR\":0.85,\"USD\":1.0,\"GBP\":0.75,\"JPY\":110.25,\"AUD\":1.35,\"CAD\":1.25,\"CHF\":0.95,\"CNY\":6.45}}");
            }
        });

        return webClientMock;
    }

    @Bean
    @Primary
    public CurrencyService currencyService(WebClient webClient) {
        return new CurrencyExchangeRateService(webClient);
    }
}