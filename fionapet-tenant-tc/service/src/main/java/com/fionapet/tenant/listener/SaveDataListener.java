package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.*;
import com.fionapet.tenant.tc.service.ArbitrageLogService;
import com.fionapet.tenant.tc.service.OrderBookPriceService;
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
public class SaveDataListener {

    @Autowired
    XchangeService xchangeService;

    @Autowired
    OrderBookPriceService orderBookPriceService;

    @Autowired
    ArbitrageLogService arbitrageLogService;

    @Autowired
    ApplicationContext applicationContext;

    @EventListener
    @Async
    public void save(SaveDataEvent saveDataEvent) {
        Exchange exchange = saveDataEvent.getExchange();
        TriangleCurrency triangleCurrency = saveDataEvent.getTriangleCurrency();
        ArbitrageLog arbitrageLog = new ArbitrageLog(triangleCurrency);


        float arbitrage = triangleCurrency.posCyclePrice();

        if (Arbitrage.TYPE_NEG.equals(saveDataEvent.getType())){
            arbitrage = triangleCurrency.negCyclePrice();
        }

        arbitrageLogService.save(arbitrageLog);


        if (arbitrage > 0) {

            arbitrageLog.setPecentage(arbitrage);
            arbitrageLog.setArbitrage(arbitrage);
            arbitrageLog.setType(saveDataEvent.getType());
            arbitrageLog.setExchangeId(exchange.getId());

            saveData(arbitrageLog, triangleCurrency);
        }

    }

    private void saveData(ArbitrageLog arbitrageLog, TriangleCurrency triangleCurrency) {
        triangleCurrency.getBaseQuoteOrderBookPrice().setArbitrageLogId(arbitrageLog.getId());
        triangleCurrency.getBaseMidOrderBookPrice().setArbitrageLogId(arbitrageLog.getId());
        triangleCurrency.getQuoteMidOrderBookPrice().setArbitrageLogId(arbitrageLog.getId());

        orderBookPriceService.save(triangleCurrency.getBaseQuoteOrderBookPrice());
        orderBookPriceService.save(triangleCurrency.getBaseMidOrderBookPrice());
        orderBookPriceService.save(triangleCurrency.getQuoteMidOrderBookPrice());
    }

}
