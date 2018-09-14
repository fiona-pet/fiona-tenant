package com.fionapet.tenant.listener;

import lombok.Getter;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.springframework.context.ApplicationEvent;

@Getter
public class ExchangeOrderEvent extends ApplicationEvent {
    private OrderBook orderBook;


    public ExchangeOrderEvent(Object source,
                              OrderBook orderBook) {
        super(source);
        this.orderBook = orderBook;
    }
}
