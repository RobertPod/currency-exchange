package com.exchange.app;

import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class RatesProviderTestsWithMyOwnStab {


    @Test
    void getExchangeRateList() {
    }
}

class MyStab implements ForeignExchangeRatesApiClient {

    @Override
    public ExchangeRates getLatestRates() {
        return null;
    }

    @Override
    public List<ExchangeRates> getLatestRatesForCurrencies(List<String> symbols) {
        return null;
    }

    @Override
    public ExchangeRates getLatestRates(String base) {
        return null;
    }

    @Override
    public ExchangeRates getHistoricalRates(DateTime date) {
        return null;
    }

    @Override
    public List<ExchangeRates> getHistoricalRates(DateTime start_at, DateTime end_at) {
        return null;
    }

    @Override
    public List<ExchangeRates> getHistoricalRates(DateTime start_at, DateTime end_at,
        List<String> symbols) {
        return null;
    }

    @Override
    public List<ExchangeRates> getHistoricalRates(DateTime start_at, DateTime end_at, String base) {
        return null;
    }
}
