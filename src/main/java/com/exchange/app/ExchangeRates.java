package com.exchange.app;

import java.util.HashMap;
import org.joda.time.DateTime;

import java.util.Map;

public class ExchangeRates {
    private String base;
    private DateTime date;
    private Map<String, Double> rates;

    public ExchangeRates(String base, DateTime date, Map<String, Double> rates) {
        this.base = base;
        this.date = date;
        this.rates = rates;
    }

    public Double get(String currency) {
        return rates.get(currency);
    }

    public DateTime getDate() {
        return date;
    }

    static class RatesForCurrencyForDayBuilder {
        private String currency;
        private Map<String, Double> rates;
        private DateTime date;

        public RatesForCurrencyForDayBuilder based(String currency) {
            this.currency = currency;
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

        public RatesForCurrencyForDayBuilder forDay(DateTime date) {
            this.date = date;
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

