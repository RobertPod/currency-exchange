package com.exchange.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.exchange.app.ExchangeRates.RatesForCurrencyForDayBuilder;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.data.Offset;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;


public class RatesProviderTestsWithMyOwnStab {


    @Test
    void getExchangeRateList() {
        //given
        ForeignExchangeRatesApiClient apiClient = new MyStabAndMock();
        RatesProvider provider = new RatesProvider(apiClient);

        //when
        Double difference = provider
            .getPriceDifferenceFor100USDInTimeRange(DateTime.now().minusDays(10),
                DateTime.now(), "PLN");

        //then
        assertThat(((MyStabAndMock)apiClient).getNumberOfMethodCalls()).isEqualTo(1);
        assertThat(difference).isEqualTo(888.0, Offset.offset(0.01));
    }
}


class MyStabAndMock implements ForeignExchangeRatesApiClient {

    private int numberOfMethodCalls = 0;

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
    public List<ExchangeRates> getHistoricalRates(DateTime startAt, DateTime endAt, String base) {
        numberOfMethodCalls++;
        return new ArrayList<ExchangeRates>() {{
            add(new RatesForCurrencyForDayBuilder()
                .based(base)
                .forDay(startAt)
                .addRate("USD", 1.11)
                .addRate("EUR", 0.11)
                .build()
            );
            add(new RatesForCurrencyForDayBuilder()
                .based(base)
                .forDay(endAt)
                .addRate("USD", 9.99)
                .addRate("EUR", 0.99)
                .build()
            );
        }};
    }

    public int getNumberOfMethodCalls() {
        return numberOfMethodCalls;
    }
}
