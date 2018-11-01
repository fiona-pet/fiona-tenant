package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.Arbitrage;
import com.fionapet.tenant.tc.entity.ArbitrageLog;
import com.fionapet.tenant.tc.entity.OrderBookPrice;
import com.fionapet.tenant.tc.entity.TrianglePair;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableAsync
public class PlaceOrderListener {
    @EventListener
    @Async
    public void placeOrder(PlaceOrderEvent placeOrderEvent) {
        String arbitrageType = placeOrderEvent.getArbitrageType();

        /**
         * p1
         */
         OrderBookPrice quoteMidOrderBookPrice = placeOrderEvent.getQuoteMidOrderBookPrice();
        /**
         * p2
         */
         OrderBookPrice baseQuoteOrderBookPrice = placeOrderEvent.getBaseQuoteOrderBookPrice();
        /**
         * p3
         */
         OrderBookPrice baseMidOrderBookPrice = placeOrderEvent.getBaseMidOrderBookPrice();

        try {
            if (Arbitrage.TYPE_NEG.equals(arbitrageType)){
                log.info("buy base/mid size:{}, price:{}", baseMidOrderBookPrice.getBuyAmount(), baseMidOrderBookPrice.getBuy());
                log.info("sell base/quote size:{}, price:{}", baseQuoteOrderBookPrice.getSellAmount(), baseQuoteOrderBookPrice.getSell());
                log.info("sell quote/mid size:{}, price:{}", quoteMidOrderBookPrice.getSellAmount(), quoteMidOrderBookPrice.getSell());
            }else if (Arbitrage.TYPE_POS.equals(arbitrageType)){
                log.info("buy quote/mid size:{}, price:{}", quoteMidOrderBookPrice.getBuyAmount(), quoteMidOrderBookPrice.getBuy());
                log.info("buy base/quote size:{}, price:{}", baseQuoteOrderBookPrice.getBuyAmount(), baseQuoteOrderBookPrice.getBuy());
                log.info("sell base/mid size:{}, price:{}", baseMidOrderBookPrice.getSellAmount(), baseMidOrderBookPrice.getSell());
            }

        } finally {


        }
    }
}
