package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.Exchange;
import lombok.Getter;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.context.ApplicationEvent;

@Getter
public class ExchangeOrderEvent extends ApplicationEvent {
    private Exchange exchange;

    public ExchangeOrderEvent(Object source, Exchange exchange) {
        super(source);
        this.exchange = exchange;
    }
}
