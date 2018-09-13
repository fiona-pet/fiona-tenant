package com.fionapet.tenant.tc.task;

import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.service.ExchangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

@EnableScheduling
@Component
@Slf4j
public class SyncExchangeOrderBook {
    @Autowired
    private ExchangeService exchangeService;

    @Scheduled(cron = "0/5 * * * * ?") //每5秒执行一次
    public void doSomething() throws Exception {
        List<Exchange> exchangeList = exchangeService.list();

        exchangeList.stream().forEach(new Consumer<Exchange>() {
            @Override
            public void accept(Exchange exchange) {
                log.info("获取OrderBook:{}...", exchange);

                log.info("获取OrderBook");
            }
        });

    }
}
