package com.fionapet.tenant.tc.task;

import com.fionapet.tenant.listener.ExchangeEvent;
import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.entity.TriangleCurrency;
import com.fionapet.tenant.tc.service.ArbitrageLogService;
import com.fionapet.tenant.tc.service.ExchangeService;
import com.fionapet.tenant.xchange.XchangeService;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@EnableScheduling
@Configurable
@Component
//@EnableAsync
@Slf4j
@EnableCaching
public class ExchangeSymbolsTask {

    @Autowired
    private ExchangeService exchangeService;

    @Autowired
    private XchangeService xchangeService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ArbitrageLogService arbitrageLogService;


//    @Scheduled(cron = "0/500 * * * * * ?") //每5秒执行一次
//    @Async
    @Scheduled(fixedRate = 500)
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

                    Set<TriangleCurrency>
                            trianglePairs =
                            xchangeService.grenCurrencyPair(Currency.USD, currencyPairs);

                    trianglePairs.stream().forEach(new Consumer<TriangleCurrency>() {
                        @Override
                        public void accept(TriangleCurrency triangleCurrency) {
                                applicationContext
                                        .publishEvent(
                                                new ExchangeEvent(this, exchange,
                                                        triangleCurrency));

                        }
                    });

                    log.info("获取 三角链 数据.");
                } catch (Exception e) {
                    log.debug("{} 获取数据异常", exchange);
                }

                log.info(xchangeService.printStopWatch());
            }
        });
    }
}
