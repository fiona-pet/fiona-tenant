package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.entity.TriangleCurrency;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Setter
@Getter
public class PlaceOrderEvent extends ApplicationEvent {

    Exchange exchange;
    TriangleCurrency triangleCurrency;

    public PlaceOrderEvent(Object source,Exchange exchange,
                           TriangleCurrency triangleCurrency) {
        super(source);
        this.exchange = exchange;
        this.triangleCurrency = triangleCurrency;
    }


}
