package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.Arbitrage;
import com.fionapet.tenant.tc.entity.ArbitrageLog;
import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.entity.TrianglePair;
import com.fionapet.tenant.tc.service.ArbitrageLogService;
import com.fionapet.tenant.tc.service.TopOneOrderBookService;
import com.fionapet.tenant.xchange.XchangeService;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
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
    public void exchange(ExchangeEvent exchangeEvent) {
        Exchange exchange = exchangeEvent.getExchange();
        TrianglePair trianglePair = exchangeEvent.getTrianglePair();

        if (!updateTrianglePairData(exchange, trianglePair)) {
            return;
        }

        applicationContext
                .publishEvent(
                        new ArbitrageEvent(this, exchange,
                                          trianglePair,Arbitrage.TYPE_POS));
        applicationContext
                .publishEvent(
                        new ArbitrageEvent(this, exchange,
                                           trianglePair,Arbitrage.TYPE_NEG));

    }

    /**
     * 更新 币对 盘口 数据
     * @param exchange
     * @param trianglePair
     * @return
     */
    private boolean updateTrianglePairData(Exchange exchange, final TrianglePair trianglePair) {
        OrderBook
                baseQuotePairOrderBook =
                getOrderBook(exchange.getInstanceName(), new CurrencyPair(trianglePair.getBaseCur(), trianglePair.getQuoteCur()));

        if (null == baseQuotePairOrderBook) {
            return false;
        }

        trianglePair.setMarketPrice(baseQuotePairOrderBook);

        OrderBook
                baseMidPairOrderBook =
                getOrderBook(exchange.getInstanceName(), new CurrencyPair(trianglePair.getBaseCur(), trianglePair.getMidCur()));

        trianglePair.setBaseMidPrice(baseMidPairOrderBook);

        OrderBook
                quoteMidPairOrderBook =
                getOrderBook(exchange.getInstanceName(), new CurrencyPair(trianglePair.getQuoteCur(), trianglePair.getMidCur()));

        trianglePair.setQuoteMidPrice(quoteMidPairOrderBook);

        return true;
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
