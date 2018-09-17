package com.fionapet.tenant.tc.task;

import com.fionapet.tenant.listener.ArbitrageEvent;
import com.fionapet.tenant.listener.ExchangeOrderEvent;
import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.entity.TrianglePair;
import com.fionapet.tenant.tc.service.ExchangeService;
import com.fionapet.tenant.tc.service.TopOneOrderBookService;
import com.fionapet.tenant.xchange.XchangeService;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@EnableScheduling
@Component
@Slf4j
public class ArbitrageTask {

    @Autowired
    private ExchangeService exchangeService;

    @Autowired
    private XchangeService xchangeService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    TopOneOrderBookService topOneOrderBookService;

    @Scheduled(cron = "0/5 * * * * ?") //每5秒执行一次
    public void grenCurrencyPair() throws Exception {
        List<Exchange> exchangeList = exchangeService.list();

        exchangeList.stream().forEach(new Consumer<Exchange>() {
            @Override
            public void accept(Exchange exchange) {
                log.info("获取 三角链 数据准备 :{}...", exchange);

                List<CurrencyPair>
                        currencyPairs =
                        xchangeService.getExchangeSymbols(exchange.getInstanceName());

                List<TrianglePair> trianglePairs = xchangeService.grenCurrencyPair(Currency.USD, currencyPairs);


                trianglePairs.stream().forEach(new Consumer<TrianglePair>() {
                    @Override
                    public void accept(TrianglePair trianglePair) {
                        TopOneOrderBook topOneOrderBook = topOneOrderBookService.findByExchangeIdAndCurrencyPair(exchange.getId(), trianglePair.getConvertPair().toString());

                        if (null != topOneOrderBook){
                            trianglePair.setConvertPairSellPrice(topOneOrderBook.getAskPrice());

                            topOneOrderBook = topOneOrderBookService.findByExchangeIdAndCurrencyPair(exchange.getId(), trianglePair.getFromBasePair().toString());
                            trianglePair.setFromBasePairSellPrice(topOneOrderBook.getAskPrice());

                            topOneOrderBook = topOneOrderBookService.findByExchangeIdAndCurrencyPair(exchange.getId(), trianglePair.getToBasePair().toString());
                            trianglePair.setToBasePairBuyPrice(topOneOrderBook.getBidPrice());

                            applicationContext
                                    .publishEvent(new ArbitrageEvent(this, trianglePair));
                        }
                    }
                });

                log.info("获取 三角链 数据准备");
            }
        });
    }
}
