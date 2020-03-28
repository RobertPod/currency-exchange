package com.exchange.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

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
        List<String> currencies = Arrays.asList(EUR, SEK, USD);

        Mockito.when(apiClient.getLatestRates(anyString())).thenAnswer(
            (Answer<ExchangeRates>) invocationOnMock -> {
                Object base = invocationOnMock.getArgument(0);
                if (currencies.contains(base)) {
                    return exchangeRates;
                } else {
                    throw new CurrencyNotSupportedException("Not supported: " + base);
                }
            }
        );

        RatesProvider provider = new RatesProvider(apiClient);

        //when
        Double rate = provider.getExchangeRate(Currency.getInstance(SEK), Currency.getInstance(USD));

        //then
        assertThat(10.30).isEqualTo(rate);
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

    //    ====
    @Test
    void shouldGetHistoricalRatesOnlyOnce() {
        //given
        ForeignExchangeRatesApiClient apiClient = Mockito.mock(ForeignExchangeRatesApiClient.class);
        Mockito.when(apiClient.getHistoricalRates(DateTime.now().minusDays(7), DateTime.now())).
            thenReturn(new ArrayList<>());

        RatesProvider provider = new RatesProvider(apiClient);

        //when
        provider.getExchangeRateListInEUR(Currency.getInstance(SEK), DateTime.now().minusDays(7),
            DateTime.now());

        //then
        Mockito.verify(apiClient, times(1)).getHistoricalRates(any(), any());
    }

    @Test
    void shouldGetHistoricalRatesReturnProperData() {
        //given
        ForeignExchangeRatesApiClient apiClient = Mockito.mock(ForeignExchangeRatesApiClient.class);
        List<ExchangeRates> exchangeRates = prepareTestData();
        Mockito.when(apiClient.getHistoricalRates(any(), any()))
            .thenReturn(exchangeRates);

        RatesProvider provider = new RatesProvider(apiClient);

        //when
        Map<DateTime, Double> rates = provider
            .getExchangeRateListInEUR(Currency.getInstance(SEK), DateTime.now().minusDays(7),
                DateTime.now());

        //then
        assertThat(rates.size()).isEqualTo(6);
        assertThat(thePriceFrom5DaysAgoIsDefined(rates)).isTrue();
        assertThat(thePriceFrom2DaysAgo(rates)).isEqualTo(14.19);
    }

    private boolean thePriceFrom5DaysAgoIsDefined(
        Map<DateTime, Double> rates) {
        return rates.containsKey(DateTime.now().minusDays(5).withTimeAtStartOfDay());
    }

    private double thePriceFrom2DaysAgo(Map<DateTime, Double> rates) {
        return rates.get(DateTime.now().minusDays(2).withTimeAtStartOfDay());
    }

    private List<ExchangeRates> prepareTestData() {
        return new ArrayList<ExchangeRates>() {{
            add(new RatesForCurrencyForDayBuilder().basedEUR()
                .forDay(0).addRate(USD, ranCurr(0.14, 0.16)).addRate(SEK, ranCurr(13.1, 14.99))
                .build());
            add(new RatesForCurrencyForDayBuilder().basedEUR()
                .forDay(-1).addRate(USD, ranCurr(0.14, 0.16)).addRate(SEK, ranCurr(13.1, 14.99))
                .build());
            add(new RatesForCurrencyForDayBuilder().basedEUR()
                .forDay(-2).addRate(USD, 1.13).addRate(SEK, 14.19).build());
            add(new RatesForCurrencyForDayBuilder().basedEUR()
                .forDay(-3).addRate(USD, ranCurr(0.14, 0.16)).addRate(SEK, ranCurr(13.1, 14.99))
                .build());
            add(new RatesForCurrencyForDayBuilder().basedEUR()
                .forDay(-4).addRate(USD, ranCurr(0.14, 0.16)).addRate(SEK, ranCurr(13.1, 14.99))
                .build());
            add(new RatesForCurrencyForDayBuilder().basedEUR()
                .forDay(-5).addRate(USD, ranCurr(0.14, 0.16)).addRate(SEK, ranCurr(13.1, 14.99))
                .build());
        }};
    }

    private double ranCurr(double min, double max) {
        Random r = new Random();
        return min + (max - min) * r.nextDouble();
    }
//    ====

    private ExchangeRates initializeExchangeRates() {
        rates.put(USD, 1.22);
        rates.put(SEK, 10.30);
        return initializeExchangeRates(DateTime.now(), rates);
    }

    private ExchangeRates initializeExchangeRates(DateTime date,
        Map<String, Double> rates) {
        return new ExchangeRates(RatesProviderTests.EUR, date, rates);
    }

    private static class RatesForCurrencyForDayBuilder {
        private String currency;
        private Map<String, Double> rates;
        private DateTime date;

        public RatesForCurrencyForDayBuilder basedEUR() {
            currency = EUR;
            return this;
        }

        public RatesForCurrencyForDayBuilder addRate(String foreignCurrency, Double rate) {
            if (rates == null) {
                rates = new HashMap<>();
            }
            if (currency != null && !currency.equals(foreignCurrency)) {
                rates.put(foreignCurrency, rate);
            }
            return this;
        }

        /**
         * diff from today
         */
        public RatesForCurrencyForDayBuilder forDay(int day) {
            DateTime dateTime = DateTime.now().withTimeAtStartOfDay();
            if (day > 0) {
                dateTime = dateTime.plusDays(day);
            }
            if (day < 0) {
                dateTime = dateTime.minusDays(-day);
            }
            date = dateTime;
            return this;
        }

        public ExchangeRates build() {
            if (date == null) {
                this.date = DateTime.now();
            }
            return new ExchangeRates(currency, date, rates);
        }
    }
}