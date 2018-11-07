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
    OrderBookPriceService orderBookPriceService;

    @Autowired
    XchangeService xchangeService;

    @EventListener
    @Async
    public void placeOrder(PlaceOrderEvent placeOrderEvent) {
        Exchange exchange = placeOrderEvent.getExchange();
        TriangleCurrency triangleCurrency = placeOrderEvent.getTriangleCurrency();

        ArbitrageLog arbitrageLog = new ArbitrageLog(triangleCurrency);
        if (triangleCurrency.posCyclePrice() > 0) {

            //TODO 实际成交
//            BigDecimal qSize = BigDecimal.valueOf(Math.min(
//                    triangleCurrency.getBaseQuoteOrderBookPrice().getBidAmount().doubleValue(),
//                    triangleCurrency.getQuoteMidOrderBookPrice().getAskAmount().doubleValue()));
//
//            if (qSize.doubleValue() > 0.001){
//                qSize = BigDecimal.valueOf(0.001);
//            }
//
//            // q/m buy qSize q
//            LimitOrder limitOrder = xchangeService.buy(exchange.getInstanceName(), qSize,
//                                                    triangleCurrency.getQuoteMidPair(),
//                                                    triangleCurrency.getQuoteMidOrderBookPrice().getAsk());
//            if (null == limitOrder){
//                return;
//            }
//
//            int retry = 0;
//            while(retry < 3){
//                retry++;
//
//                BitstampBalance.Balance qBalance = xchangeService.getBalance(exchange.getInstanceName(), triangleCurrency.getQuoteCur().getDisplayName());
//
//                if (qBalance.getAvailable().doubleValue() <= 0d){
//                    continue;
//                }
//
//                // b/q sell qSize
//                BitstampOrder bqOrder =  xchangeService.sell(exchange.getInstanceName(), qBalance.getAvailable(),
//                                    triangleCurrency.getBaseQuotePair(),
//                                    triangleCurrency.getBaseQuoteOrderBookPrice().getBid());
//                if (null != bqOrder) {
//
//                    // 账号中 b 的数量
//                    BitstampBalance.Balance
//                            bBalance =
//                            xchangeService.getBalance(exchange.getInstanceName(),
//                                                      triangleCurrency.getBaseCur()
//                                                              .getDisplayName());
//
//                    if (bBalance.getAvailable().doubleValue() <= 0d) {
//                        continue;
//                    }
//
//                    // b/m sell
//                    xchangeService
//                            .sell(exchange.getInstanceName(), bBalance.getAvailable(),
//                                  triangleCurrency.getBaseMidPair(),
//                                  triangleCurrency.getBaseMidOrderBookPrice().getBid());
//                }
//
//                if (retry==3){
//                    break;
//                }
//            }

            //TODO 取消订单

            arbitrageLog.setPecentage(triangleCurrency.posCyclePrice());
            arbitrageLog.setArbitrage(triangleCurrency.posCyclePrice());
            arbitrageLog.setType(Arbitrage.TYPE_POS);
            arbitrageLog.setExchangeId(exchange.getId());

            saveData(arbitrageLog, triangleCurrency);
        } else if (triangleCurrency.negCyclePrice() > 0) {
            arbitrageLog.setPecentage(triangleCurrency.negCyclePrice());
            arbitrageLog.setArbitrage(triangleCurrency.negCyclePrice());
            arbitrageLog.setType(Arbitrage.TYPE_NEG);
            arbitrageLog.setExchangeId(exchange.getId());

            saveData(arbitrageLog, triangleCurrency);
        }

        arbitrageLog.setPecentage(triangleCurrency.posCyclePrice());
        arbitrageLog.setArbitrage(triangleCurrency.posCyclePrice());
        arbitrageLog.setType(Arbitrage.TYPE_POS);
        arbitrageLog.setExchangeId(exchange.getId());

        saveData(arbitrageLog, triangleCurrency);
    }

    private void saveData(ArbitrageLog arbitrageLog, TriangleCurrency triangleCurrency) {
        arbitrageLogService.save(arbitrageLog);

        triangleCurrency.getBaseQuoteOrderBookPrice().setArbitrageLogId(arbitrageLog.getId());
        triangleCurrency.getBaseMidOrderBookPrice().setArbitrageLogId(arbitrageLog.getId());
        triangleCurrency.getQuoteMidOrderBookPrice().setArbitrageLogId(arbitrageLog.getId());

        orderBookPriceService.save(triangleCurrency.getBaseQuoteOrderBookPrice());
        orderBookPriceService.save(triangleCurrency.getBaseMidOrderBookPrice());
        orderBookPriceService.save(triangleCurrency.getQuoteMidOrderBookPrice());
    }
}
