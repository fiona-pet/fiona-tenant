package com.fionapet.tenant.xchange;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.marketdata.params.CurrencyPairsParam;
import org.knowm.xchange.utils.jackson.CurrencyPairDeserializer;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class XchangeService {

    /**
     *
     * @param instanceName
     * @param currencyPairStr eg: BTCUSD
     *
     * @return
     */
    public OrderBook getOrderBookByCurrencyPair(String instanceName, String currencyPairStr)
            throws IOException {
        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(instanceName);

        MarketDataService marketDataService = exchange.getMarketDataService();

        CurrencyPair currencyPair = CurrencyPairDeserializer.getCurrencyPairFromString(currencyPairStr);

        return marketDataService.getOrderBook(currencyPair);
    }

}
