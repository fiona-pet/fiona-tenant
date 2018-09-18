package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.service.ExchangeService;
import com.fionapet.tenant.tc.service.TopOneOrderBookService;
import com.fionapet.tenant.xchange.XchangeService;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class ExchangeOrderUpdateListener {

    @Autowired
    XchangeService xchangeService;

    @Autowired
    TopOneOrderBookService topOneOrderBookService;

    @EventListener
    public void update(ExchangeOrderEvent exchangeOrderEvent) throws IOException {
        //获取 订单数据
        OrderBook
                orderBook =
                xchangeService
                        .getOrderBook(exchangeOrderEvent.getExchange().getInstanceName(),
                                      exchangeOrderEvent.getCurrencyPair());

        TopOneOrderBook topOneOrderBook = xchangeService.toTopOneOrderBook(orderBook);
        topOneOrderBook.setCurrencyPair(exchangeOrderEvent.getCurrencyPair().toString());
        topOneOrderBook.setExchangeId(exchangeOrderEvent.getExchange().getId());

        TopOneOrderBook
                topOneOrderBookOld =
                topOneOrderBookService
                        .findByExchangeIdAndCurrencyPair(exchangeOrderEvent.getExchange().getId(),
                                                         topOneOrderBook.getCurrencyPair());
        if (null == topOneOrderBookOld) {
            topOneOrderBookOld = topOneOrderBook;
        } else {
            topOneOrderBookOld.setAskPrice(topOneOrderBook.getAskPrice());
            topOneOrderBookOld.setBidPrice(topOneOrderBook.getBidPrice());
        }

        topOneOrderBookService.save(topOneOrderBookOld);

        //输出行情信息
        log.debug("@EventListener订单信息：{}", topOneOrderBookOld);
    }
}
