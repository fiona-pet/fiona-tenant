package com.fionapet.tenant.tc.service;

import com.fionapet.tenant.listener.PlaceOrderListener;
import com.fionapet.tenant.tc.entity.ArbitrageLog;
import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.entity.TriangleCurrency;
import com.fionapet.tenant.tc.repository.ArbitrageLogRepository;
import com.fionapet.tenant.xchange.XchangeService;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrder;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrderStatus;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrderStatusResponse;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ArbitrageService {

    private static final Logger log = LoggerFactory.getLogger(ArbitrageService.class);

    @Autowired
    XchangeService xchangeService;

    float off = 1f;
    float maxUsd = 50;


    @Transactional
    public void pos(Exchange exchange, TriangleCurrency
            triangleCurrency) throws InterruptedException {
        // p1(s)/p2(b) - p3(s)  ((b/m)/(q/m) - b/q) 正链

        // p2 q/m 下买单
        double qmSize = triangleCurrency.getQuoteMidOrderBookPrice().getAskAmount().doubleValue();
        double bqSize = triangleCurrency.getBaseQuoteOrderBookPrice().getAskAmount().doubleValue();

        BigDecimal
                qmOrderSize =
                BigDecimal.valueOf(Math.floor(Math.min(bqSize, qmSize) * 10000) / 10000);

        BigDecimal
                qmPrice =
                BigDecimal.valueOf(
                        triangleCurrency.getQuoteMidOrderBookPrice().getAsk().floatValue() * off);

        log.info("p2: {} buy -> p: {}, s: {}, qmOrderSize:{}",
                triangleCurrency.getQuoteMidOrderBookPrice().getCurrencyPair(),
                qmPrice,
                triangleCurrency.getQuoteMidOrderBookPrice().getAskAmount(), qmOrderSize);

        BigDecimal
                bqPrice =
                BigDecimal.valueOf(
                        triangleCurrency.getBaseQuoteOrderBookPrice().getAsk().floatValue() * off);

        BigDecimal
                bqOrderSize =
                BigDecimal.valueOf(qmOrderSize.floatValue() * bqPrice.floatValue());

        // p3 b/q 下买单
        log.info("p3: {} buy -> p: {}, s: {}, bqOrderSize:{}",
                triangleCurrency.getBaseQuoteOrderBookPrice().getCurrencyPair(),
                bqPrice,
                triangleCurrency.getBaseQuoteOrderBookPrice().getAskAmount(), bqOrderSize);

        BigDecimal
                bmPrice = triangleCurrency.getBaseMidOrderBookPrice().getBid();
        BigDecimal
                bmOrderSize =
                BigDecimal.valueOf(Math.min(bqOrderSize.floatValue(), triangleCurrency.getBaseMidOrderBookPrice().getBidAmount().floatValue()));

        // p1 b/m 下卖单
        log.info("p1: {} sell -> p: {}, s: {}, bmOrderSize:{}",
                triangleCurrency.getBaseMidOrderBookPrice().getCurrencyPair(),
                bmPrice,
                triangleCurrency.getBaseMidOrderBookPrice().getBidAmount(), bmOrderSize);

        log.info("posCyclePrice:{}", triangleCurrency.posCyclePrice());
    }

    public void negOrder(Exchange exchange, TriangleCurrency
            triangleCurrency) throws InterruptedException {
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
                        .setScale(2, RoundingMode.UP);

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
                            bmPrice.setScale(2, RoundingMode.UP));

            BigDecimal
                    bqPrice =
                    BigDecimal.valueOf(
                            triangleCurrency.getBaseQuoteOrderBookPrice().getBid().floatValue()
                                    * off);
            // p3
            SellThread
                    p3 =
                    new SellThread(exchange, "p3", bmOrderSize.floatValue(), bqPrice.floatValue(), bmOrderId,
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
                    new SellThread(exchange, "p2", qmSize, qmPrice, bmOrderId, new CurrencyPair(
                            triangleCurrency.getQuoteMidOrderBookPrice().getCurrencyPair()));
            p2.start();

            p3.join();
            p2.join();
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

        if (BitstampOrderStatus.Finished != bitstampOrderStatusResponse.getStatus()) {
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
                                        .setScale(2, RoundingMode.UP), currencyPair,
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
