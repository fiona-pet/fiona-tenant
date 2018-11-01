package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.OrderBookPrice;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Setter
@Getter
public class PlaceOrderEvent extends ApplicationEvent {
    private String arbitrageType;
    /**
     * p1
     */
    private OrderBookPrice quoteMidOrderBookPrice;
    /**
     * p2
     */
    private OrderBookPrice baseQuoteOrderBookPrice;
    /**
     * p3
     */
    private OrderBookPrice baseMidOrderBookPrice;

    public PlaceOrderEvent(Object source, String arbitrageType,
                           OrderBookPrice quoteMidOrderBookPrice,
                           OrderBookPrice baseQuoteOrderBookPrice,
                           OrderBookPrice baseMidOrderBookPrice) {
        super(source);
        this.arbitrageType = arbitrageType;
        this.quoteMidOrderBookPrice = quoteMidOrderBookPrice;
        this.baseQuoteOrderBookPrice = baseQuoteOrderBookPrice;
        this.baseMidOrderBookPrice = baseMidOrderBookPrice;
    }
}
