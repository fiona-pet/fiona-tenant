package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.*;
import com.fionapet.tenant.tc.service.ArbitrageLogService;
import com.fionapet.tenant.tc.service.OrderBookPriceService;
import com.fionapet.tenant.xchange.XchangeService;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.bitstamp.dto.account.BitstampBalance;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrder;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@EnableAsync
public class PlaceOrderListener {

    @Autowired
    ArbitrageLogService arbitrageLogService;


    @Autowired
    XchangeService xchangeService;

    @Autowired
    ApplicationContext applicationContext;

    @EventListener
    @Async
    public void placeOrder(PlaceOrderEvent placeOrderEvent) {
        Exchange exchange = placeOrderEvent.getExchange();
        TriangleCurrency triangleCurrency = placeOrderEvent.getTriangleCurrency();

        applicationContext
                .publishEvent(new SaveDataEvent(this, exchange, triangleCurrency));


        if (triangleCurrency.posCyclePrice() > 0) {

            //TODO 实际成交
//            BigDecimal qSize = BigDecimal.valueOf(Math.min(
//                    triangleCurrency.getBaseQuoteOrderBookPrice().getBidAmount().doubleValue(),
//                    triangleCurrency.getQuoteMidOrderBookPrice().getAskAmount().doubleValue()));
//
//            if (qSize.doubleValue() * triangleCurrency.getQuoteMidOrderBookPrice().getAsk().doubleValue() > 100) {
//                qSize = BigDecimal.valueOf(100 / triangleCurrency.getQuoteMidOrderBookPrice().getAsk().doubleValue());
//            }
//
//            // q/m buy qSize q
//            LimitOrder limitOrder = xchangeService.buy(exchange.getInstanceName(), qSize,
//                    triangleCurrency.getQuoteMidPair(),
//                    triangleCurrency.getQuoteMidOrderBookPrice().getAsk());
//            if (null == limitOrder) {
//                return;
//            }
//
//            int retry = 0;
//            while (retry < 3) {
//                retry++;
//
//                BitstampBalance.Balance qBalance = xchangeService.getBalance(exchange.getInstanceName(), triangleCurrency.getQuoteCur().getDisplayName());
//
//                if (qBalance.getAvailable().doubleValue() <= 0d) {
//                    continue;
//                }
//
//                // b/q sell qSize
//                BitstampOrder bqOrder = xchangeService.sell(exchange.getInstanceName(), qBalance.getAvailable(),
//                        triangleCurrency.getBaseQuotePair(),
//                        triangleCurrency.getBaseQuoteOrderBookPrice().getBid());
//                if (null != bqOrder) {
//
//                    // 账号中 b 的数量
//                    BitstampBalance.Balance
//                            bBalance =
//                            xchangeService.getBalance(exchange.getInstanceName(),
//                                    triangleCurrency.getBaseCur()
//                                            .getDisplayName());
//
//                    if (bBalance.getAvailable().doubleValue() <= 0d) {
//                        continue;
//                    }
//
//                    // b/m sell
//                    xchangeService
//                            .sell(exchange.getInstanceName(), bBalance.getAvailable(),
//                                    triangleCurrency.getBaseMidPair(),
//                                    triangleCurrency.getBaseMidOrderBookPrice().getBid());
//                }
//
//                if (retry == 3) {
//                    break;
//                }
//            }

            //TODO 取消订单


        } else if (triangleCurrency.negCyclePrice() > 0) {
            // TODO neg
            log.warn("has neg:{}", "TODO");

        }
    }


}
