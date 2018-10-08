package com.fionapet.tenant.tc.task;

import com.fionapet.tenant.listener.ArbitrageEvent;
import com.fionapet.tenant.listener.ExchangeEvent;
import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.entity.TrianglePair;
import com.fionapet.tenant.tc.service.ArbitrageLogService;
import com.fionapet.tenant.tc.service.ExchangeService;
import com.fionapet.tenant.tc.service.TopOneOrderBookService;
import com.fionapet.tenant.xchange.XchangeService;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@EnableScheduling
@Configurable
@Component
@EnableAsync
@Slf4j
public class ExchangeSymbolsTask {

    @Autowired
    private ExchangeService exchangeService;

    @Autowired
    private XchangeService xchangeService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ArbitrageLogService arbitrageLogService;

    @Scheduled(cron = "0/5 * * * * ?") //每5秒执行一次
    @Async
    public void grenCurrencyPair() {

        List<Exchange> exchangeList = exchangeService.list();

        exchangeList.stream().forEach(new Consumer<Exchange>() {
            @Override
            public void accept(Exchange exchange) {
                log.info("获取 三角链 数据准备 :{}...", exchange);

                arbitrageLogService.clean(exchange.getId());

                try {
                    List<CurrencyPair>
                            currencyPairs =
                            xchangeService.getExchangeSymbols(exchange.getInstanceName());

                    List<TrianglePair>
                            trianglePairs =
                            xchangeService.grenCurrencyPair(Currency.USD, currencyPairs);

                    trianglePairs.stream().forEach(new Consumer<TrianglePair>() {
                        @Override
                        public void accept(TrianglePair trianglePair) {
                            applicationContext
                                    .publishEvent(
                                            new ExchangeEvent(this, exchange,
                                                              trianglePair));
                        }
                    });

                    log.info("获取 三角链 数据");
                } catch (Exception e) {
                    log.debug("{} 获取数据异常", exchange);
                }
            }
        });
    }
}
