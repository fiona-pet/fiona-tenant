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
        Arbitrage arbitrage = null;
        TrianglePair trianglePair = arbitrageEvent.getTrianglePair();

        if (Arbitrage.TYPE_POS.equals(arbitrageEvent.getArbitrageType())) {
            arbitrage = trianglePair.posArbitrage();
        } else {
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

}
