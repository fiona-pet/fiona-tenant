package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.Arbitrage;
import com.fionapet.tenant.tc.entity.ArbitrageLog;
import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.entity.TrianglePair;
import com.fionapet.tenant.tc.service.ArbitrageLogService;
import com.fionapet.tenant.tc.service.TopOneOrderBookService;
import com.fionapet.tenant.xchange.XchangeService;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ArbitrageListener {

    @Autowired
    XchangeService xchangeService;

    @Autowired
    TopOneOrderBookService topOneOrderBookService;

    @Autowired
    ArbitrageLogService arbitrageLogService;

    @EventListener
    public void arbitrage(ArbitrageEvent arbitrageEvent) {
        Exchange exchange = arbitrageEvent.getExchange();
        TrianglePair trianglePair = arbitrageEvent.getTrianglePair();

        if (!updateTrianglePairData(exchange, trianglePair)) {
            return;
        }

        arbitrage(Arbitrage.TYPE_POS, trianglePair);
        arbitrage(Arbitrage.TYPE_NEG, trianglePair);

    }

    private void arbitrage(String type,TrianglePair trianglePair){
        Arbitrage arbitrage = null;
        if (Arbitrage.TYPE_POS.equals(type)) {
            arbitrage = trianglePair.posArbitrage();
        }else {
            arbitrage = trianglePair.negArbitrage();
        }

        if (null != arbitrage) {
            ArbitrageLog arbitrageLog = new ArbitrageLog(trianglePair);
            arbitrageLog.setArbitrage(arbitrage.getArbitrage());
            arbitrageLog.setPecentage(arbitrage.getPecentage());
            arbitrageLog.setType(arbitrage.getType());
            arbitrageLog.setExchangeId(1l);

            arbitrageLogService.save(arbitrageLog);
        }
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
