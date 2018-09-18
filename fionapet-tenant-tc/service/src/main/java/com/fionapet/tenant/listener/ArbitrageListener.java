package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.ArbitrageLog;
import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.entity.TrianglePair;
import com.fionapet.tenant.tc.service.ArbitrageLogService;
import com.fionapet.tenant.tc.service.TopOneOrderBookService;
import com.fionapet.tenant.xchange.XchangeService;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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
        TopOneOrderBook
                topOneOrderBook =
                topOneOrderBookService
                        .findByExchangeIdAndCurrencyPair(arbitrageEvent.getExchageId(),
                                                         arbitrageEvent.getTrianglePair()
                                                                 .getConvertPair().toString());

        TrianglePair trianglePair = arbitrageEvent.getTrianglePair();

        if (null != topOneOrderBook) {
            trianglePair.setConvertPairSellPrice(topOneOrderBook.getAskPrice());

            topOneOrderBook =
                    topOneOrderBookService
                            .findByExchangeIdAndCurrencyPair(arbitrageEvent.getExchageId(),
                                                             trianglePair.getFromBasePair()
                                                                     .toString());
            trianglePair.setFromBasePairSellPrice(topOneOrderBook.getAskPrice());

            topOneOrderBook =
                    topOneOrderBookService
                            .findByExchangeIdAndCurrencyPair(arbitrageEvent.getExchageId(),
                                                             trianglePair.getToBasePair()
                                                                     .toString());
            trianglePair.setToBasePairBuyPrice(topOneOrderBook.getBidPrice());

            Float arbitrage = arbitrageEvent.getTrianglePair().arbitrage();

            ArbitrageLog arbitrageLog = new ArbitrageLog(arbitrageEvent.getTrianglePair());
            arbitrageLog.setArbitrage(arbitrage);
            arbitrageLog.setExchangeId(arbitrageEvent.getExchageId());
            arbitrageLogService.save(arbitrageLog);

            log.debug("套利市场:{}, 套利币对:{}, 金额:{}", arbitrageEvent.getExchageId(),
                      arbitrageEvent.getTrianglePair(), arbitrage);
        }

    }
}
