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
import java.util.List;

@Service
public class XchangeService {

    /**
     * 订单 信息
     * @param instanceName
     * @param currencyPair eg: BTC_USD
     *
     * @return
     */
    public OrderBook getOrderBookByCurrencyPair(String instanceName, CurrencyPair currencyPair)
            throws IOException {
        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(instanceName);

        MarketDataService marketDataService = exchange.getMarketDataService();

        return marketDataService.getOrderBook(currencyPair);
    }

    /**
     * 获取 市场 币对列表
     * @param instanceName
     * @return
     */
    public List<CurrencyPair> getExchangeSymbols(String instanceName){
        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(instanceName);
        return exchange.getExchangeSymbols();
    }



}
