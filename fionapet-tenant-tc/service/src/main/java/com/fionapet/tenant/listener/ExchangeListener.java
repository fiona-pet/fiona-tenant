package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.*;
import com.fionapet.tenant.tc.service.ArbitrageLogService;
import com.fionapet.tenant.tc.service.TopOneOrderBookService;
import com.fionapet.tenant.xchange.XchangeService;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableAsync
public class ExchangeListener {

    @Autowired
    XchangeService xchangeService;

    @Autowired
    TopOneOrderBookService topOneOrderBookService;

    @Autowired
    ArbitrageLogService arbitrageLogService;

    @Autowired
    ApplicationContext applicationContext;

    @EventListener
    @Async
    public void exchange(ExchangeEvent exchangeEvent) {
        Exchange exchange = exchangeEvent.getExchange();
        TriangleCurrency triangleCurrency = exchangeEvent.getTriangleCurrency();

        if (updateTrianglePairData(exchange, triangleCurrency)) {

            PlaceOrderEvent placeOrderEvent = new PlaceOrderEvent(this, exchange, triangleCurrency);

            applicationContext
                    .publishEvent(placeOrderEvent);
        }
    }

    /**
     * 更新 币对 盘口 数据
     *
     * @param exchange
     * @param triangleCurrency
     * @return
     */
    private boolean updateTrianglePairData(Exchange exchange, final TriangleCurrency triangleCurrency) {
        OrderBook
                baseQuotePairOrderBook =
                getOrderBook(exchange.getInstanceName(), triangleCurrency.getBaseQuotePair());

        if (null == baseQuotePairOrderBook) {
            return false;
        }

        OrderBookPrice baseQuoteOrderBookPrice = toOrderBookPrice(triangleCurrency.getBaseQuotePair(), baseQuotePairOrderBook);

        if (null == baseQuoteOrderBookPrice){
            return false;
        }

        triangleCurrency.setBaseQuoteOrderBookPrice(baseQuoteOrderBookPrice);

        OrderBook
                baseMidPairOrderBook =
                getOrderBook(exchange.getInstanceName(), triangleCurrency.getBaseMidPair());

        if (null == baseMidPairOrderBook) {
            return false;
        }

        OrderBookPrice baseMidOrderBookPrice = toOrderBookPrice(triangleCurrency.getBaseMidPair(), baseMidPairOrderBook);

        if (null == baseMidOrderBookPrice) {
            return false;
        }

        triangleCurrency.setBaseMidOrderBookPrice(baseMidOrderBookPrice);

        OrderBook
                quoteMidPairOrderBook =
                getOrderBook(exchange.getInstanceName(), triangleCurrency.getQuoteMidPair());


        OrderBookPrice quoteMidOrderBookPrice = toOrderBookPrice(triangleCurrency.getQuoteMidPair(), quoteMidPairOrderBook);

        triangleCurrency.setQuoteMidOrderBookPrice(quoteMidOrderBookPrice);

        return true;
    }

    private OrderBookPrice toOrderBookPrice(CurrencyPair currencyPair, OrderBook orderBook) {
        OrderBookPrice orderBookPrice = new OrderBookPrice();

        if (null == orderBook){
            return null;
        }

        orderBookPrice.setBid(orderBook.getBids().get(0).getLimitPrice());
        orderBookPrice.setBidAmount(orderBook.getBids().get(0).getRemainingAmount());
        orderBookPrice.setAsk(orderBook.getAsks().get(0).getLimitPrice());
        orderBookPrice.setAskAmount(orderBook.getAsks().get(0).getRemainingAmount());
        orderBookPrice.setCurrencyPair(currencyPair.toString());

        return orderBookPrice;
    }


    private OrderBook getOrderBook(String instanceName, CurrencyPair currencyPair) {
        //获取 订单数据
        OrderBook
                orderBook =
                null;
        try {
            orderBook = xchangeService
                    .getOrderBook(instanceName,
                            currencyPair);


        } catch (Exception e) {
            log.warn("{} getOrderBook {} error!", instanceName, currencyPair);
        }

        return orderBook;
    }
}
