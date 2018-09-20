package com.fionapet.tenant.tc.task;

import com.fionapet.tenant.listener.ExchangeOrderEvent;
import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.service.ExchangeService;
import com.fionapet.tenant.tc.service.TopOneOrderBookService;
import com.fionapet.tenant.xchange.XchangeService;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;


//@EnableScheduling
//@Configurable
//@Component
//@EnableAsync
@Slf4j
public class SyncExchangeOrderBook {

    @Autowired
    private ExchangeService exchangeService;

    @Autowired
    private XchangeService xchangeService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    TopOneOrderBookService topOneOrderBookService;

    @Scheduled(cron = "0/5 * * * * ?") //每5秒执行一次
    @Async
    public void update() throws Exception {
        List<Exchange> exchangeList = exchangeService.list();

        exchangeList.stream().forEach(new Consumer<Exchange>() {
            @Override
            public void accept(Exchange exchange) {
                log.info("获取OrderBook:{}...", exchange);

                applicationContext
                        .publishEvent(new ExchangeOrderEvent(this,
                                                             exchange));

                log.info("获取OrderBook");
            }
        });
    }
}
