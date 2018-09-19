package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.service.ExchangeService;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Component
@EnableAsync
public class ExchangeOrderUpdateListener {

    @Autowired
    XchangeService xchangeService;

    @Autowired
    TopOneOrderBookService topOneOrderBookService;

    public static final Map<Long, Boolean> IS_RUN = new HashMap<>();


    @EventListener
    @Async
    public void update(ExchangeOrderEvent exchangeOrderEvent){
        Exchange exchange = exchangeOrderEvent.getExchange();

        Boolean isRun = IS_RUN.get(exchange.getId());

        log.debug("{}：数据同步中", exchange);

        if (null != isRun && isRun){
            log.debug("{}：数据同步中...", exchange);
            return;
        }

        IS_RUN.put(exchange.getId(), true);

        try {
            List<CurrencyPair>
                    currencyPairs =
                    xchangeService.getExchangeSymbols(exchange.getInstanceName());

            List<TopOneOrderBook> topOneOrderBooks = new ArrayList<>();

            currencyPairs.stream().forEach(new Consumer<CurrencyPair>() {
                @Override
                public void accept(CurrencyPair currencyPair) {
                    try {
                        //获取 订单数据
                        OrderBook
                                orderBook =
                                xchangeService
                                        .getOrderBook(exchange.getInstanceName(),
                                                      currencyPair);

                        TopOneOrderBook
                                topOneOrderBook =
                                xchangeService.toTopOneOrderBook(orderBook);
                        topOneOrderBook
                                .setCurrencyPair(currencyPair.toString());
                        topOneOrderBook.setExchangeId(exchangeOrderEvent.getExchange().getId());

                        TopOneOrderBook
                                topOneOrderBookOld =
                                topOneOrderBookService
                                        .findByExchangeIdAndCurrencyPair(
                                                exchangeOrderEvent.getExchange().getId(),
                                                topOneOrderBook.getCurrencyPair());

                        if (null != topOneOrderBookOld) {
                            topOneOrderBook.setId(topOneOrderBookOld.getId());
                        }

                        topOneOrderBooks.add(topOneOrderBook);

                        //输出行情信息
                        log.debug("@EventListener订单信息：{}", topOneOrderBook);
                    } catch (Exception e) {
                        log.warn("from {} get order book error!", e);
                    }

                }
            });

            topOneOrderBookService.save(topOneOrderBooks);
            IS_RUN.put(exchange.getId(), false);
        } catch (Exception e) {
            IS_RUN.put(exchange.getId(), false);
            log.warn("exchange:{}", exchange, e);
        }


    }
}
