package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.*;
import com.fionapet.tenant.tc.service.ArbitrageLogService;
import com.fionapet.tenant.tc.service.ArbitrageService;
import com.fionapet.tenant.xchange.XchangeService;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrder;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrderStatus;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrderStatusResponse;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
@EnableAsync
public class PlaceOrderListener {

    @Autowired
    ArbitrageLogService arbitrageLogService;


    @Autowired
    XchangeService xchangeService;

    @Autowired
    ArbitrageService arbitrageService;

    @Autowired
    ApplicationContext applicationContext;

    @EventListener
//    @Async
    public void placeOrder(PlaceOrderEvent placeOrderEvent) {
        Exchange exchange = placeOrderEvent.getExchange();
        TriangleCurrency triangleCurrency = placeOrderEvent.getTriangleCurrency();


        if (triangleCurrency.posCyclePrice() > 0) {
            log.info("POS:{}", "DOING...");
            try {
                arbitrageService.pos(exchange, triangleCurrency);

                applicationContext
                        .publishEvent(new SaveDataEvent(this, exchange, triangleCurrency, Arbitrage.TYPE_POS));      } catch (InterruptedException e) {
                log.warn("posOrder", e);
            }
            log.info("POS:{}", "DONE");
        } else if (triangleCurrency.negCyclePrice() > 0) {
            log.info("NEG:{}", "DOING...");
            try {
                arbitrageService.negOrder(exchange, triangleCurrency);

                applicationContext
                        .publishEvent(new SaveDataEvent(this, exchange, triangleCurrency, Arbitrage.TYPE_NEG));
            } catch (InterruptedException e) {
                log.warn("negOrder", e);
            }
            log.info("NEG:{}", "DONE");
        }

    }




}
