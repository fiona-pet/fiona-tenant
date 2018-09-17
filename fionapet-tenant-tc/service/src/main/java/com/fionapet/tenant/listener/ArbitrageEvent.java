package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.TrianglePair;
import lombok.Getter;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.springframework.context.ApplicationEvent;

@Getter
public class ArbitrageEvent extends ApplicationEvent {
    private TrianglePair trianglePair;

    public ArbitrageEvent(Object source, TrianglePair trianglePair) {
        super(source);
        this.trianglePair = trianglePair;
    }
}