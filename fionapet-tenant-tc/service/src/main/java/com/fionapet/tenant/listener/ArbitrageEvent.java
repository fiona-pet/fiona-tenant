package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.entity.TrianglePair;
import lombok.Getter;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.springframework.context.ApplicationEvent;

@Getter
public class ArbitrageEvent extends ApplicationEvent {
    private Exchange exchange;
    private TrianglePair trianglePair;

    public ArbitrageEvent(Object source, Exchange exchange,
                          TrianglePair trianglePair) {
        super(source);
        this.exchange = exchange;
        this.trianglePair = trianglePair;
    }
}
