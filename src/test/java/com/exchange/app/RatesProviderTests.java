package com.exchange.app;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class RatesProviderTests {

    private static final String SEK = "SEK";
    private static final String USD = "USD";
    private static final String EUR = "EUR";

    private Map<String, Double> rates;

    @BeforeEach
    void setUp() {
        rates = new HashMap<String, Double>() {
        };
    }

    @Test
    @DisplayName("For default currency (EUR) returns USD rate")
    void test1() {

        //given
        ForeignExchangeRatesApiClient apiClient = Mockito.mock(ForeignExchangeRatesApiClient.class);
        ExchangeRates exchangeRates = initializeExchangeRates();
        Mockito.when(apiClient.getLatestRates()).thenReturn(exchangeRates);

        RatesProvider provider = new RatesProvider(apiClient);

        //when
        Double rateUSD = provider.getExchangeRateInEUR(Currency.getInstance(USD));

        //then
        assertThat(exchangeRates.get(USD)).isEqualTo(rateUSD);
    }

    @Test
    @DisplayName("For default currency (EUR) returns all rates")
    void test2() {
        //given
        ForeignExchangeRatesApiClient apiClient = Mockito.mock(ForeignExchangeRatesApiClient.class);
        ExchangeRates exchangeRates = initializeExchangeRates();
        Mockito.when(apiClient.getLatestRates()).thenReturn(exchangeRates);

        RatesProvider provider = new RatesProvider(apiClient);

        //when
        Double rateSEK = provider.getExchangeRateInEUR(Currency.getInstance(SEK));
        Double rateUSD = provider.getExchangeRateInEUR(Currency.getInstance(USD));

        //then
        assertAll(
                () -> assertEquals(exchangeRates.get(USD), rateUSD, "USD rate should be included"),
                () -> assertEquals(exchangeRates.get(SEK), rateSEK, "SEK rate should be included")
        );
    }

    @Test
    void shouldReturnCurrencyExchangeRatesForOtherCurrency() {
        //given
        ForeignExchangeRatesApiClient apiClient = Mockito.mock(ForeignExchangeRatesApiClient.class);
        ExchangeRates exchangeRates = initializeExchangeRates();
        List<String> currencies = Arrays.asList(new String[]{EUR, SEK, USD});

        Mockito.when(apiClient.getLatestRates(anyString())).thenAnswer(
                new Answer<ExchangeRates>() {

                    @Override
                    public ExchangeRates answer(InvocationOnMock invocationOnMock) throws Throwable {
                        Object base = invocationOnMock.getArgument(0);
                        if (currencies.contains(base)) {
                            return exchangeRates;
                        } else {
                            throw new CurrencyNotSupportedException("Not supported: " + base);
                        }
                    }
                }
        );

        RatesProvider provider = new RatesProvider(apiClient);

        //then
        assertThatThrownBy(() -> {
            provider.getExchangeRate(Currency.getInstance(SEK), Currency.getInstance("CAD"));
        }).hasMessage("Not supported: CAD");
    }

    @Test
    void shouldThrowExceptionWhenCurrencyNotSupported() {
        //given
        ForeignExchangeRatesApiClient apiClient = Mockito.mock(ForeignExchangeRatesApiClient.class);
        Mockito.when(apiClient.getLatestRates()).thenThrow(new IllegalArgumentException());

        RatesProvider provider = new RatesProvider(apiClient);

        //then

        CurrencyNotSupportedException actual =
                assertThrows(CurrencyNotSupportedException.class,
                        () -> provider.getExchangeRateInEUR(Currency.getInstance("CHF")));

        assertEquals("Currency is not supported: CHF", actual.getMessage());
    }

    @Test
    void shouldGetRatesOnlyOnce() {
        //given
        ForeignExchangeRatesApiClient apiClient = Mockito.mock(ForeignExchangeRatesApiClient.class);
        ExchangeRates exchangeRates = initializeExchangeRates();
        Mockito.when(apiClient.getLatestRates()).thenReturn(exchangeRates);

        RatesProvider provider = new RatesProvider(apiClient);

        //when
        provider.getExchangeRateInEUR(Currency.getInstance(SEK));

        //then
        Mockito.verify(apiClient).getLatestRates();
    }

    private ExchangeRates initializeExchangeRates() {
        rates.put(USD, 1.22);
        rates.put(SEK, 10.30);
        return initializeExchangeRates(EUR, DateTime.now(), rates);
    }

    private ExchangeRates initializeExchangeRates(String base) {
        rates.put(EUR, 1.22);
        rates.put(SEK, 10.30);
        return initializeExchangeRates(base, DateTime.now(), rates);
    }

    private ExchangeRates initializeExchangeRates(String base, DateTime date, Map<String, Double> rates) {
        return new ExchangeRates(base, date, rates);
    }

}