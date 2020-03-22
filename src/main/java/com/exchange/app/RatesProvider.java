package com.exchange.app;

import java.util.Currency;
import java.util.List;
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
            throw new CurrencyNotSupportedException(
                "Currency is not supported: " + requested.getCurrencyCode());
        }
    }

    public Double getExchangeRate(Currency requested, Currency exchanged) {
        return apiClient.getLatestRates(exchanged.getCurrencyCode())
            .get(requested.getCurrencyCode());
    }

    public Double getPriceDifferenceFor100USDInTimeRange(DateTime startAt, DateTime stopAt,
        String currency) {
        List<ExchangeRates> exchangeRatesList = apiClient
            .getHistoricalRates(startAt, stopAt, currency);
        return (exchangeRatesList.get(exchangeRatesList.size() - 1).get("USD") - exchangeRatesList
            .get(0).get("USD")) * 100;
    }
}
