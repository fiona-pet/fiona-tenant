package com.fionapet.tenant.tc.task;

import com.fionapet.tenant.listener.ExchangeOrderEvent;
import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.service.ExchangeService;
import com.fionapet.tenant.xchange.XchangeService;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

@EnableScheduling
@Component
@Slf4j
public class SyncExchangeOrderBook {

    @Autowired
    private ExchangeService exchangeService;

    @Autowired
    private XchangeService xchangeService;

    @Autowired
    ApplicationContext applicationContext;

    @Scheduled(cron = "0/5 * * * * ?") //每5秒执行一次
    public void doSomething() throws Exception {
        List<Exchange> exchangeList = exchangeService.list();

        exchangeList.stream().forEach(new Consumer<Exchange>() {
            @Override
            public void accept(Exchange exchange) {
                log.info("获取OrderBook:{}...", exchange);

                List<CurrencyPair>
                        currencyPairs =
                        xchangeService.getExchangeSymbols(exchange.getInstanceName());

                currencyPairs.stream().forEach(new Consumer<CurrencyPair>() {
                    @Override
                    public void accept(CurrencyPair currencyPair) {
                        try {
                            OrderBook
                                    orderBook =
                                    xchangeService
                                            .getOrderBook(exchange.getInstanceName(), currencyPair);
                            applicationContext
                                    .publishEvent(new ExchangeOrderEvent(this, orderBook, exchange.getId(),  currencyPair));
                        } catch (IOException e) {
                            log.warn("from {} get order book error!", e);
                        }

                    }
                });

                log.info("获取OrderBook");
            }
        });

    }
}
