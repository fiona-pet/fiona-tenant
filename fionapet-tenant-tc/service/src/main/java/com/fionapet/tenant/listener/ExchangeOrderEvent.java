package com.fionapet.tenant.listener;

import lombok.Getter;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.springframework.context.ApplicationEvent;

@Getter
public class ExchangeOrderEvent extends ApplicationEvent {
    private OrderBook orderBook;
    private Long exchangeId;
    private CurrencyPair currencyPair;

    public ExchangeOrderEvent(Object source, OrderBook orderBook, Long exchangeId,
                              CurrencyPair currencyPair) {
        super(source);
        this.orderBook = orderBook;
        this.exchangeId = exchangeId;
        this.currencyPair = currencyPair;
    }
}
