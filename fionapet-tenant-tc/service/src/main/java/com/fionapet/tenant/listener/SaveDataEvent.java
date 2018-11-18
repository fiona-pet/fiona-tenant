package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.entity.TriangleCurrency;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class SaveDataEvent  extends ApplicationEvent {
    private Exchange exchange;
    private TriangleCurrency triangleCurrency;

    public SaveDataEvent(Object source, Exchange exchange, TriangleCurrency triangleCurrency) {
        super(source);
        this.exchange = exchange;
        this.triangleCurrency = triangleCurrency;
    }
}
