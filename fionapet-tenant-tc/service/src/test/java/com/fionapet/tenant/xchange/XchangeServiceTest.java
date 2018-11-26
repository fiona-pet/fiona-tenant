package com.fionapet.tenant.xchange;

import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.entity.OrderBookPrice;
import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.entity.TriangleCurrency;
import com.fionapet.tenant.tc.service.ArbitrageService;
import com.fionapet.tenant.tc.service.ExchangeService;
import com.fionapet.tenant.tc.service.OrderBookPriceService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.bitstamp.dto.account.BitstampBalance;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrder;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrderStatus;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrderStatusResponse;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.ExchangeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * XchangeService Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>九月 13, 2018</pre>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class XchangeServiceTest {

    @Autowired
    XchangeService xchangeService;

    @Autowired
    private ExchangeService exchangeService;

    @Autowired
    OrderBookPriceService orderBookPriceService;

    @Autowired
    ArbitrageService arbitrageService;

    String instanceName = "org.knowm.xchange.bitstamp.BitstampExchange";


    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getOrderBookByCurrencyPair(String instanceName, String currencyPairStr)
     */
    @Test
    public void testGetOrderBookByCurrencyPair() throws Exception {
        TopOneOrderBook orderBook = xchangeService.getOrderBookByCurrencyPair(instanceName,
                                                                              CurrencyPair.BTC_USD);

        Assert.assertNotNull(orderBook);
    }

    /**
     * Method: getExchangeSymbols(String instanceName)
     */
    @Test
    public void testGetExchangeSymbols() throws Exception {
        List<CurrencyPair> currencyPairList = xchangeService.getExchangeSymbols(instanceName);

        log.info("currencyPairList:{}", currencyPairList);

        Assert.assertTrue(currencyPairList.size() > 0);
    }

    @Test
    public void testGrenCurrencyPair() throws Exception {
        List<CurrencyPair> currencyPairList = xchangeService.getExchangeSymbols(instanceName);

        Set<TriangleCurrency>
                trianglePairs =
                xchangeService.grenCurrencyPair(Currency.USD, currencyPairList);

        log.info("trianglePairs:{}", trianglePairs);
    }

    @Test
    public void testBuy() throws IOException {
        xchangeService.buy(instanceName, new BigDecimal(".001"), CurrencyPair.BTC_USD,
                           new BigDecimal("6727"));

        log.info("open orders:{}", xchangeService.getOpenOrders(instanceName));
    }

    @Test
    public void testSell() throws IOException {
        float orderSize = 0.01f;
        float price = 4000.68f;

        log.info("orderSize:{}, price:{}", BigDecimal.valueOf(orderSize)
                .setScale(2, RoundingMode.UP), BigDecimal.valueOf(price).setScale(2, RoundingMode.UP));

//        BitstampOrder
//                bitstampOrder =
//                xchangeService.sell(instanceName, BigDecimal.valueOf(orderSize)
//                                            .setScale(2, RoundingMode.DOWN), CurrencyPair.BTC_EUR,
//                                    BigDecimal.valueOf(price).setScale(2, RoundingMode.UP));
//
//        log.info("open orders:{}, order:{}", xchangeService.getOpenOrders(instanceName),
//                 bitstampOrder);
//
//        log.info(xchangeService.printStopWatch());
    }

    @Test
    public void testGetBalance() {
        BitstampBalance.Balance balance = xchangeService.getBalance(instanceName, "BTC");
        log.info("balance:{}", balance);
    }


    @Test
    public void testPosOrder() throws InterruptedException {
        final TriangleCurrency
                triangleCurrency =
                orderBookPriceService.getByArbitrageLogId(5240281l);
        Exchange exchange = exchangeService.getById(1l);
        arbitrageService.pos(exchange, triangleCurrency);
    }

    @Test
    public void testNegOrder() throws InterruptedException {
        float off = 1f;
        float maxUsd = 50;
        final TriangleCurrency
                triangleCurrency =
                orderBookPriceService.getByArbitrageLogId(21733l);

        Assert.assertTrue(triangleCurrency.negCyclePrice() > 0);

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
                        .setScale(6, RoundingMode.UP);

        log.info("p1: {} buy -> p: {}, s: {}, bmOrderSize:{}",
                 triangleCurrency.getBaseMidOrderBookPrice().getCurrencyPair(),
                 bmPrice,
                 triangleCurrency.getBaseMidOrderBookPrice().getAskAmount(), bmOrderSize);

        String bmOrderResult = "";
        String
                bmOrderId = null;
        try {
            bmOrderId =
                    xchangeService.buy(instanceName, bmOrderSize
                            ,
                                       new CurrencyPair(triangleCurrency.getBaseMidOrderBookPrice()
                                                                .getCurrencyPair()),
                                       bmPrice);
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
            Thread.sleep(200);
            bitstampOrderStatusResponse = xchangeService.getOrder(instanceName, bmOrderId);
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
                    new SellThread("p3", bmOrderSize.floatValue(), bqPrice.floatValue(), bmOrderId,
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
                    new SellThread("p2", qmSize, qmPrice, bmOrderId, new CurrencyPair(
                            triangleCurrency.getQuoteMidOrderBookPrice().getCurrencyPair()));
            p2.start();

            p3.join();
            p2.join();
        } else {
            try {
                xchangeService.cancel(instanceName, bmOrderId);
            } catch (IOException e) {
                log.warn("bm cancel error!", e);
            }
        }

    }

    class SellThread extends Thread {

        private float orderSize;
        private float price;
        private String orderId;
        private CurrencyPair currencyPair;

        public SellThread(String name, float orderSize, float price, String orderId,
                          CurrencyPair currencyPair) {
            super(name + ":" + orderId);
            this.orderSize = orderSize;
            this.price = price;
            this.orderId = orderId;
            this.currencyPair = currencyPair;
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
                        xchangeService.sell(instanceName, BigDecimal.valueOf(orderSize)
                                                    .setScale(6, RoundingMode.UP), currencyPair,
                                            BigDecimal.valueOf(price).setScale(6, RoundingMode.UP));

                BitstampOrderStatusResponse bitstampOrderStatusResponse = null;

                try {
                    Thread.sleep(200);
                    bitstampOrderStatusResponse =
                            xchangeService.getOrder(instanceName, bitstampOrder.getId() + "");
                } catch (Exception e) {
                    log.warn("bm buy error!", e);
                }

                if (null == bitstampOrderStatusResponse) {
                    return;
                }

                if (BitstampOrderStatus.Finished != bitstampOrderStatusResponse.getStatus()) {
                    xchangeService.cancel(instanceName, bitstampOrder.getId() + "");
                    xchangeService.sell(instanceName,
                                        BigDecimal.valueOf(orderSize).setScale(6, RoundingMode.UP),
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

    @Test
    public void testGetOrder() throws IOException {
        String bmOrderId = "2406305842";
        BitstampOrderStatusResponse
                bitstampOrderStatusResponse =
                xchangeService.getOrder(instanceName, bmOrderId);

        log.info("getTransactions:{}", bitstampOrderStatusResponse.getTransactions()[0].getBch());
    }


    @Test
    public void testCurrency() {
        TriangleCurrency triangleCurrency = new TriangleCurrency();
        OrderBookPrice bq = new OrderBookPrice();

        bq.setAsk(BigDecimal.valueOf(0.42488f));
        bq.setCurrencyPair("XRP/EUR");

        triangleCurrency.setBaseQuoteOrderBookPrice(bq);

        OrderBookPrice bm = new OrderBookPrice();

        bm.setBid(BigDecimal.valueOf(0.49f));
        bm.setCurrencyPair("XRP/USD");

        triangleCurrency.setBaseMidOrderBookPrice(bm);

        OrderBookPrice qm = new OrderBookPrice();

        qm.setAsk(BigDecimal.valueOf(1.14309f));
        qm.setCurrencyPair("EUR/USD");

        triangleCurrency.setQuoteMidOrderBookPrice(qm);

        log.info("posCyclePrice:{}, negCyclePrice:{}", triangleCurrency.posCyclePrice(),
                 triangleCurrency.negCyclePrice());

    }


    @Test
    public void testDataCurrency() {
        TriangleCurrency triangleCurrency = new TriangleCurrency();
        OrderBookPrice bq = new OrderBookPrice();

        bq.setAsk(BigDecimal.valueOf(0.00851172f));

        bq.setBid(BigDecimal.valueOf(0.00849f));
        bq.setCurrencyPair("LTC/BTC");

        triangleCurrency.setBaseQuoteOrderBookPrice(bq);

        OrderBookPrice bm = new OrderBookPrice();

        bm.setBid(BigDecimal.valueOf(55.35f));

        bm.setAsk(BigDecimal.valueOf(55.47f));
        bm.setCurrencyPair("LTC/USD");

        triangleCurrency.setBaseMidOrderBookPrice(bm);

        OrderBookPrice qm = new OrderBookPrice();

        qm.setAsk(BigDecimal.valueOf(6519.52f));
        qm.setBid(BigDecimal.valueOf(6517.35f));

        qm.setCurrencyPair("BTC/USD");

        triangleCurrency.setQuoteMidOrderBookPrice(qm);

        log.info("posCyclePrice:{}, negCyclePrice:{}", triangleCurrency.posCyclePrice(),
                 triangleCurrency.negCyclePrice());

    }


} 
