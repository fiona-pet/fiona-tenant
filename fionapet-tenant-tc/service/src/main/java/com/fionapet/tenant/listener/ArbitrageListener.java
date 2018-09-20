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

        TopOneOrderBook
                convertPairOrderBook =
                getTopOneOrderBook(exchange.getInstanceName(), trianglePair.getConvertPair());

        if (null == convertPairOrderBook) {
            return;
        }

        trianglePair.setConvertPairSellPrice(convertPairOrderBook.getAskPrice());
        trianglePair.setConvertPairRemainingAmount(convertPairOrderBook.getAskRemainingAmount());

        TopOneOrderBook
                fromBasePairOrderBook =
                getTopOneOrderBook(exchange.getInstanceName(), trianglePair.getFromBasePair());

        trianglePair.setFromBasePairSellPrice(fromBasePairOrderBook.getAskPrice());
        trianglePair.setFromBasePairRemainingAmount(fromBasePairOrderBook.getAskRemainingAmount());

        TopOneOrderBook
                toBasePairOrderBook =
                getTopOneOrderBook(exchange.getInstanceName(), trianglePair.getToBasePair());

        trianglePair.setToBasePairBuyPrice(toBasePairOrderBook.getBidPrice());
        trianglePair.setToBasePairRemainingAmount(toBasePairOrderBook.getBidRemainingAmount());

        Arbitrage arbitrage = trianglePair.arbitrage();

        ArbitrageLog arbitrageLog = new ArbitrageLog(arbitrageEvent.getTrianglePair());
        arbitrageLog.setArbitrage(arbitrage.getArbitrage());
        arbitrageLog.setArbitragePecentage(arbitrage.getPecentage());
        arbitrageLog.setExchangeId(exchange.getId());

        arbitrageLogService.save(arbitrageLog);

        log.debug("套利市场:{}, 套利币对:{}, 金额:{}", exchange.getId(),
                  arbitrageEvent.getTrianglePair(), arbitrage);
    }


    private TopOneOrderBook getTopOneOrderBook(String instanceName, CurrencyPair currencyPair) {
        TopOneOrderBook
                topOneOrderBook = null;
        //获取 订单数据
        OrderBook
                orderBook =
                null;
        try {
            orderBook = xchangeService
                    .getOrderBook(instanceName,
                                  currencyPair);

            topOneOrderBook =
                    xchangeService.toTopOneOrderBook(orderBook);

        } catch (IOException e) {
            log.warn("{} getTopOneOrderBook {} error!", instanceName, currencyPair, e);
        }

        return topOneOrderBook;
    }
}
