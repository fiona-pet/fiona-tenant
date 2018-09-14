package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.springframework.context.event.EventListener;

@Slf4j
public class ExchangeOrderUpdateListener {

    @EventListener
    public void update(ExchangeOrderEvent exchangeOrderEvent)
    {
        //获取 订单数据
        OrderBook orderBook = exchangeOrderEvent.getOrderBook();


        //输出行情信息
        log.debug("@EventListener订单信息：{}", orderBook);
    }
}
