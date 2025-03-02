package com.dexwin.currencyconverter.controller;

import com.dexwin.currencyconverter.config.CurrencyConverterTestConfig;
import org.assertj.core.matcher.AssertionMatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static java.lang.Double.parseDouble;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(CurrencyConverterTestConfig.class)
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void should_convert_EUR_to_USD_with_rate_greater_than_1() throws Exception {
        this.mockMvc.perform(get("/currencies/convert?source=EUR&target=USD&amount=1"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        new AssertionMatcher<>() {
                            @Override
                            public void assertion(String value) throws AssertionError {
                                assertThat(parseDouble(value)).isGreaterThan(1.0);
                            }
                        }));
    }

    @Test
    public void should_convert_USD_to_EUR_with_rate_less_than_1() throws Exception {
        this.mockMvc.perform(get("/currencies/convert?source=USD&target=EUR&amount=1"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        new AssertionMatcher<>() {
                            @Override
                            public void assertion(String value) throws AssertionError {
                                assertThat(parseDouble(value)).isLessThan(1.0);
                            }
                        }));
    }
}