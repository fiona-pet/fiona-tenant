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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
public class ExchangeOrderUpdateListener {

    @Autowired
    XchangeService xchangeService;

    @Autowired
    TopOneOrderBookService topOneOrderBookService;

    @EventListener
    @Transactional
    public void update(ExchangeOrderEvent exchangeOrderEvent) throws IOException {

        Exchange exchange = exchangeOrderEvent.getExchange();
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
        } catch (Exception e) {
            log.warn("exchange:{}", exchange, e);
        }


    }
}
