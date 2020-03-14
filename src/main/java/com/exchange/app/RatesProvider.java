package com.exchange.app;

import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.joda.time.DateTime;

public class RatesProvider {
    private ForeignExchangeRatesApiClient apiClient;

    public RatesProvider(ForeignExchangeRatesApiClient apiClient) {

        this.apiClient = apiClient;
    }

    public Double getExchangeRateInEUR(Currency requested) {
        try {
            return apiClient.getLatestRates().get(requested.getCurrencyCode());
        } catch (IllegalArgumentException e) {
            throw new CurrencyNotSupportedException("Currency is not supported: " + requested.getCurrencyCode());
        }
    }

    public Double getExchangeRate(Currency requested, Currency exchanged) {
        return apiClient.getLatestRates(exchanged.getCurrencyCode()).get(requested.getCurrencyCode());
    }

    public Map<DateTime, Double> getExchangeRateListInEUR(Currency requested, DateTime startAt,
        DateTime endAt) {
        List<ExchangeRates> exchangeRateses;

        try {
            exchangeRateses = apiClient.getHistoricalRates(startAt, endAt);
        } catch (IllegalArgumentException e) {
            throw new CurrencyNotSupportedException(
                "Currency " + requested.getCurrencyCode()
                    + " is not supported, or or incorrect date range");
        }

        TreeMap<DateTime, Double> map = exchangeRateses.stream()
            .collect(Collectors
                .toMap(ExchangeRates::getDate,
                    exchangeRates -> exchangeRates.get(requested.getCurrencyCode()), (a, b) -> b,
                    TreeMap::new));
        return map;
    }
}
