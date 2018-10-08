package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.entity.TrianglePair;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ExchangeEvent extends ApplicationEvent {
    private Exchange exchange;
    private TrianglePair trianglePair;

    public ExchangeEvent(Object source, Exchange exchange,
                         TrianglePair trianglePair) {
        super(source);
        this.exchange = exchange;
        this.trianglePair = trianglePair;
    }
}
