package org.poo.e_banking.helpers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class ExchangeRateManager {

    private final Map<String, Double> exchangeMap;

    public ExchangeRateManager() {
        this.exchangeMap = new HashMap<>();
    }

    /**
     * Add a new exchange rate to the map.
     *
     * @param fromCurrency The currency to convert from.
     * @param toCurrency   The currency to convert to.
     * @param rate         The exchange rate.
     */
    public void addExchangeRate(final String fromCurrency, final String toCurrency,
                                final double rate) {
        String directKey = fromCurrency + "-" + toCurrency;
        exchangeMap.put(directKey, rate);

        String reverseKey = toCurrency + "-" + fromCurrency;
        exchangeMap.putIfAbsent(reverseKey, 1 / rate);
    }

    /**
     * Get the exchange rate between two currencies.
     * If the exchange rate is not directly available, it will be computed.
     *
     * @param fromCurrency The currency to convert from.
     * @param toCurrency   The currency to convert to.
     * @return The exchange rate between the two currencies.
     */
    public Double getExchangeRate(final String fromCurrency, final String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return 1.0;
        }

        String directKey = fromCurrency + "-" + toCurrency;
        if (exchangeMap.containsKey(directKey)) {
            return exchangeMap.get(directKey);
        }

        for (String intermediateCurrency : getCurrencies()) {
            String firstKey = fromCurrency + "-" + intermediateCurrency;
            String secondKey = intermediateCurrency + "-" + toCurrency;

            if (exchangeMap.containsKey(firstKey) && exchangeMap.containsKey(secondKey)) {
                double firstRate = exchangeMap.get(firstKey);
                double secondRate = exchangeMap.get(secondKey);
                double computedRate = firstRate * secondRate;

                exchangeMap.put(directKey, computedRate);

                return computedRate;
            }
        }
        return null;
    }

    private Iterable<String> getCurrencies() {
        return exchangeMap.keySet().stream()
                .map(key -> key.split("-"))
                .flatMap(Arrays::stream)
                .distinct()
                .toList();
    }
}
