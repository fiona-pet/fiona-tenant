package com.fionapet.tenant.xchange;

import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.entity.TrianglePair;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.marketdata.params.CurrencyPairsParam;
import org.knowm.xchange.utils.jackson.CurrencyPairDeserializer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class XchangeService {

    /**
     * 订单 信息
     * @param instanceName
     * @param currencyPair eg: BTC_USD
     *
     * @return
     */
    public TopOneOrderBook getOrderBookByCurrencyPair(String instanceName, CurrencyPair currencyPair)
            throws IOException {
        OrderBook orderBook = getOrderBook(instanceName, currencyPair);

        TopOneOrderBook topOneOrderBook = new TopOneOrderBook();

        topOneOrderBook.setCurrencyPair(currencyPair.toString());

        topOneOrderBook.setBidPrice(orderBook.getBids().get(0).getLimitPrice().floatValue());
        topOneOrderBook.setAskPrice(orderBook.getAsks().get(0).getLimitPrice().floatValue());

        return topOneOrderBook;
    }

    public OrderBook getOrderBook(String instanceName, CurrencyPair currencyPair)throws IOException {
        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(instanceName);

        MarketDataService marketDataService = exchange.getMarketDataService();

        OrderBook orderBook = marketDataService.getOrderBook(currencyPair);

        return orderBook;
    }

    /**
     * 获取 市场 币对列表
     * @param instanceName
     * @return
     */
    public List<CurrencyPair> getExchangeSymbols(String instanceName){
        try {
            Exchange exchange = ExchangeFactory.INSTANCE.createExchange(instanceName);
            return exchange.getExchangeSymbols();
        }catch (Exception e){
            log.warn("getExchangeSymbols", e);
        }
        return new ArrayList<>();
    }

    /**
     * 获取 币对组合
     * @param base
     * @param currencyPairs
     * @return
     */
    public List<TrianglePair> grenCurrencyPair(final Currency base, List<CurrencyPair> currencyPairs){
        //找到 所有以 base 为定价币的 币对
        List<CurrencyPair> hasBase = currencyPairs.stream().filter(new Predicate<CurrencyPair>() {
            @Override
            public boolean test(CurrencyPair currencyPair) {
                return currencyPair.counter == base;
            }
        }).collect(Collectors.toList());

        log.debug("hasBase:{}", hasBase);

        List<TrianglePair> trianglePairs = new ArrayList<>();

        for (int i = 0; i < hasBase.size(); i++){
            for (int j = i+1; j < hasBase.size(); j++) {
                TrianglePair trianglePair = new TrianglePair();

                CurrencyPair p1 = hasBase.get(i);
                CurrencyPair p2 = hasBase.get(j);
                CurrencyPair p3 = new CurrencyPair(p2.base, p1.base);

                trianglePair.setFromBasePair(p1);
                trianglePair.setToBasePair(p2);
                trianglePair.setConvertPair(p3);

                TrianglePair trianglePair2 = new TrianglePair();

                trianglePair2.setFromBasePair(trianglePair.getToBasePair());
                trianglePair2.setFromBasePair(trianglePair.getToBasePair());
                trianglePair2.setToBasePair(trianglePair.getFromBasePair());
                trianglePair2.setConvertPair(new CurrencyPair(p1.base, p2.base));

                trianglePairs.add(trianglePair);
                trianglePairs.add(trianglePair2);
            }
        }

        return trianglePairs;
    }


    public TopOneOrderBook toTopOneOrderBook(OrderBook orderBook) {
        TopOneOrderBook topOneOrderBook = new TopOneOrderBook();

        topOneOrderBook.setBidPrice(orderBook.getBids().get(0).getLimitPrice().floatValue());
        topOneOrderBook.setBidRemainingAmount(orderBook.getBids().get(0).getRemainingAmount().floatValue());

        topOneOrderBook.setAskPrice(orderBook.getAsks().get(0).getLimitPrice().floatValue());
        topOneOrderBook.setAskRemainingAmount(orderBook.getAsks().get(0).getRemainingAmount().floatValue());

        return topOneOrderBook;
    }
}
