package com.fionapet.tenant.listener;

import com.fionapet.tenant.tc.entity.*;
import com.fionapet.tenant.tc.service.ArbitrageLogService;
import com.fionapet.tenant.xchange.XchangeService;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrder;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrderStatus;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrderStatusResponse;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
            try {
                negOrder(exchange, triangleCurrency);
            } catch (InterruptedException e) {
                log.warn("negOrder", e);
            }

        }
    }

    public void negOrder(Exchange exchange, TriangleCurrency
            triangleCurrency) throws InterruptedException {
        float off = 1f;
        float maxUsd = 50;

        // p3(s) - p1(b)/p2(s) (b/q - (b/m)/(q/m)) 逆链

        // p1 b/m 下买单
        double bqSize = triangleCurrency.getBaseQuoteOrderBookPrice().getBidAmount().doubleValue();
        double bmSize = triangleCurrency.getBaseMidOrderBookPrice().getAskAmount().doubleValue();
        BigDecimal
                bmOrderSize =
                BigDecimal.valueOf(Math.floor(Math.min(bqSize, bmSize) * 10000) / 10000);

        BigDecimal
                bmPrice =
                BigDecimal.valueOf(
                        triangleCurrency.getBaseMidOrderBookPrice().getAsk().floatValue() * off);

        bmOrderSize =
                BigDecimal
                        .valueOf(Math.min(maxUsd / bmPrice.floatValue(), bmOrderSize.floatValue()))
                        .setScale(2, RoundingMode.DOWN);

        log.info("p1: {} buy -> p: {}, s: {}, bmOrderSize:{}",
                 triangleCurrency.getBaseMidOrderBookPrice().getCurrencyPair(),
                 bmPrice,
                 triangleCurrency.getBaseMidOrderBookPrice().getAskAmount(), bmOrderSize);

        String bmOrderResult = "";
        String
                bmOrderId = null;
        try {
            bmOrderId =
                    xchangeService.buy(exchange.getInstanceName(), bmOrderSize
                            ,
                                       new CurrencyPair(triangleCurrency.getBaseMidOrderBookPrice()
                                                                .getCurrencyPair()),
                                       bmPrice.setScale(2, RoundingMode.DOWN));

            BigDecimal
                    bqPrice =
                    BigDecimal.valueOf(
                            triangleCurrency.getBaseQuoteOrderBookPrice().getBid().floatValue()
                            * off);

            // p3
            SellThread
                    p3 =
                    new SellThread(exchange,"p3", bmOrderSize.floatValue(), bqPrice.floatValue(), bmOrderId,
                                   new CurrencyPair(triangleCurrency.getBaseQuoteOrderBookPrice()
                                                            .getCurrencyPair()));
            p3.start();
        } catch (IOException e) {
            bmOrderResult = e.getMessage();
            log.warn("bm buy error!", e);
        }

        log.info("p1: {} buy -> p: {}, s: {}, bmOrderSize:{}, res:{}, errorMessage:{}",
                 triangleCurrency.getBaseMidOrderBookPrice().getCurrencyPair(),
                 bmPrice,
                 triangleCurrency.getBaseMidOrderBookPrice().getAskAmount(), bmOrderSize,
                 bmOrderId, bmOrderResult);

        BitstampOrderStatusResponse bitstampOrderStatusResponse = null;

        try {
            Thread.sleep(400);
            bitstampOrderStatusResponse = xchangeService.getOrder(exchange.getInstanceName(), bmOrderId);

        } catch (IOException e) {
            log.warn("bm buy error!", e);
        }

        if (null == bitstampOrderStatusResponse) {
            return;
        }

        if (BitstampOrderStatus.Finished == bitstampOrderStatusResponse.getStatus()) {
            BigDecimal
                    bqPrice =
                    BigDecimal.valueOf(
                            triangleCurrency.getBaseQuoteOrderBookPrice().getBid().floatValue()
                            * off);

            // p3
            SellThread
                    p3 =
                    new SellThread(exchange,"p3", bmOrderSize.floatValue(), bqPrice.floatValue(), bmOrderId,
                                   new CurrencyPair(triangleCurrency.getBaseQuoteOrderBookPrice()
                                                            .getCurrencyPair()));
            p3.start();

            // p2
            float qmSize = bmOrderSize.floatValue() * bqPrice.floatValue();
            float
                    qmPrice =
                    triangleCurrency.getQuoteMidOrderBookPrice().getBid().floatValue() * off;

            SellThread
                    p2 =
                    new SellThread(exchange,"p2", qmSize, qmPrice, bmOrderId, new CurrencyPair(
                            triangleCurrency.getQuoteMidOrderBookPrice().getCurrencyPair()));
            p2.start();

            p3.join();
            p2.join();
        } else {
            try {
                xchangeService.cancel(exchange.getInstanceName(), bmOrderId);
            } catch (IOException e) {
                log.warn("bm cancel error!", e);
            }
        }

        log.info(xchangeService.printStopWatch());

    }

    class SellThread extends Thread {

        private float orderSize;
        private float price;
        private String orderId;
        private CurrencyPair currencyPair;
        private Exchange exchange;

        public SellThread(Exchange exchange, String name,
                          float orderSize, float price, String orderId,
                          CurrencyPair currencyPair) {
            super(name + ":" + orderId);
            this.orderSize = orderSize;
            this.price = price;
            this.orderId = orderId;
            this.currencyPair = currencyPair;
            this.exchange = exchange;
        }

        @Override
        public void run() {
            try {
                log.info("{} -> {} sell -> p: {}, orderSize:{}", this.getName(),
                         currencyPair,
                         price,
                         orderSize, 0);

                BitstampOrder
                        bitstampOrder =
                        xchangeService.sell(exchange.getInstanceName(), BigDecimal.valueOf(orderSize)
                                                    .setScale(2, RoundingMode.DOWN), currencyPair,
                                            BigDecimal.valueOf(price).setScale(2, RoundingMode.UP));

                BitstampOrderStatusResponse bitstampOrderStatusResponse = null;

                try {
                    Thread.sleep(400);
                    bitstampOrderStatusResponse =
                            xchangeService.getOrder(exchange.getInstanceName(), bitstampOrder.getId() + "");
                } catch (Exception e) {
                    log.warn("bm buy error!", e);
                }

                if (null == bitstampOrderStatusResponse) {
                    return;
                }

                if (BitstampOrderStatus.Finished != bitstampOrderStatusResponse.getStatus()) {
                    xchangeService.cancel(exchange.getInstanceName(), bitstampOrder.getId() + "");
                    xchangeService.sell(exchange.getInstanceName(),
                                        BigDecimal.valueOf(orderSize).setScale(2, RoundingMode.UP),
                                        currencyPair);
                }

                log.info("{} -> {} sell -> p: {}, orderSize:{}, orderResult:{}", this.getName(),
                         currencyPair,
                         price,
                         orderSize, bitstampOrder);

            } catch (IOException e) {
                log.warn("sell error!", e);
            }
        }
    }


}
