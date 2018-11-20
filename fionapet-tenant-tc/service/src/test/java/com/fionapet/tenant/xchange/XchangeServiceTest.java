package com.fionapet.tenant.xchange;

import com.fionapet.tenant.tc.entity.OrderBookPrice;
import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.entity.TriangleCurrency;
import com.fionapet.tenant.tc.service.OrderBookPriceService;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.bitstamp.dto.account.BitstampBalance;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrder;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.exceptions.ExchangeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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
    OrderBookPriceService orderBookPriceService;

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
     *
     * Method: getExchangeSymbols(String instanceName)
     *
     */
    @Test
    public void testGetExchangeSymbols() throws Exception {
        List<CurrencyPair> currencyPairList = xchangeService.getExchangeSymbols(instanceName);

        log.debug("currencyPairList:{}" , currencyPairList);

        Assert.assertTrue(currencyPairList.size()>0);
    }

    @Test
    public void testGrenCurrencyPair() throws Exception {
        List<CurrencyPair> currencyPairList = xchangeService.getExchangeSymbols(instanceName);

        Set<TriangleCurrency> trianglePairs = xchangeService.grenCurrencyPair(Currency.USD, currencyPairList);

        log.info("trianglePairs:{}" , trianglePairs);
    }

    @Test
    public void testBuy() throws IOException {
        xchangeService.buy(instanceName, new BigDecimal(".001"), CurrencyPair.BTC_USD, new BigDecimal("6727"));

        log.info("open orders:{}", xchangeService.getOpenOrders(instanceName));
    }

    @Test
    public void testSell() throws IOException {
        BitstampOrder bitstampOrder = xchangeService.sell(instanceName, new BigDecimal(".001"), CurrencyPair.BTC_USD, new BigDecimal("6740"));

        log.info("open orders:{}, order:{}", xchangeService.getOpenOrders(instanceName), bitstampOrder);
    }

    @Test
    public void testGetBalance(){
       BitstampBalance.Balance balance = xchangeService.getBalance(instanceName, "BTC");
       log.info("balance:{}", balance);
    }


    @Test
    public void testNegOrder() throws InterruptedException {
        final TriangleCurrency triangleCurrency = orderBookPriceService.getByArbitrageLogId(4758809l);
        Assert.assertTrue(triangleCurrency.negCyclePrice() > 0);

        // p3(s) - p1(b)/p2(s) (b/q - (b/m)/(q/m)) 逆链

        // p3 下卖单
        double bqSize = triangleCurrency.getBaseQuoteOrderBookPrice().getBidAmount().doubleValue();
        double bmSize = triangleCurrency.getQuoteMidOrderBookPrice().getAskAmount().doubleValue();
        double bqOrderSize = Math.floor(Math.min(bqSize, bmSize) * 10000)/10000;

        log.info("p3: {} sell -> p: {}, s: {}, bqOrderSize:{}", triangleCurrency.getBaseQuoteOrderBookPrice().getCurrencyPair(), triangleCurrency.getBaseQuoteOrderBookPrice().getBid(), triangleCurrency.getBaseQuoteOrderBookPrice().getBidAmount(),bqOrderSize);

        String orderResult = "";
        BitstampOrder bitstampOrder = null;
        try {
            bitstampOrder =
                    xchangeService.sell(instanceName, BigDecimal.valueOf(bqOrderSize), new CurrencyPair(triangleCurrency.getBaseQuoteOrderBookPrice().getCurrencyPair()), triangleCurrency.getBaseQuoteOrderBookPrice().getBid());
        }catch (ExchangeException e){
            orderResult = e.getMessage();
            log.warn("sell error!", e);
        }catch (IOException e) {
            orderResult = e.getMessage();
            log.warn("sell error!", e);
        }

        log.info("p3: {} sell -> p: {}, s: {}, bqOrderSize:{}, res:{}, errorMessage:{}", triangleCurrency.getBaseQuoteOrderBookPrice().getCurrencyPair(), triangleCurrency.getBaseQuoteOrderBookPrice().getBid(), triangleCurrency.getBaseQuoteOrderBookPrice().getBidAmount(),bqOrderSize, bitstampOrder, orderResult);


        // 同时 p1 b/m 下买单 p2 下卖单
        log.info("{} buy -> p: {}, s: {} , res:{}", triangleCurrency.getBaseMidOrderBookPrice().getCurrencyPair(), triangleCurrency.getQuoteMidOrderBookPrice().getAsk(), triangleCurrency.getQuoteMidOrderBookPrice().getAskAmount(), "order res");
        log.info("{} sell -> p: {}, s: {} , res:{}", triangleCurrency.getQuoteMidOrderBookPrice().getCurrencyPair(), triangleCurrency.getQuoteMidOrderBookPrice().getBid(), triangleCurrency.getQuoteMidOrderBookPrice().getBidAmount(), "order res");


//
//
//        // 监听 p1 p3 成交情况 监听 账户存量 监听当前行情 行情消失 回滚 p1
//        log.info("{} buy -> p: {}, s: {} , res:{}", triangleCurrency.getBaseMidOrderBookPrice().getCurrencyPair(), triangleCurrency.getBaseMidOrderBookPrice().getAsk(), triangleCurrency.getBaseMidOrderBookPrice().getAskAmount(), "order res");
//
//
//        // 同时  p3 下买  p2 下卖单
//        Thread t3 = new Thread(){
//            @Override
//            public void run() {
//                // 监听 p1 成交情况 监听 账户存量 监听当前行情 行情消失 回滚 p1
//                log.info("{} sell -> p: {}, s: {} , res:{}", triangleCurrency.getBaseQuoteOrderBookPrice().getCurrencyPair(), triangleCurrency.getBaseQuoteOrderBookPrice().getAsk(), triangleCurrency.getBaseQuoteOrderBookPrice().getAskAmount(), "order res");
//            }
//        };
//
//        // 同时  p3 下买  p2 下卖单
//        Thread t1 = new Thread(){
//            @Override
//            public void run() {
//                // p2 下买单
//                log.info("{} sell -> p: {}, s: {} , res:{}", triangleCurrency.getQuoteMidOrderBookPrice().getCurrencyPair(), triangleCurrency.getQuoteMidOrderBookPrice().getAsk(), triangleCurrency.getQuoteMidOrderBookPrice().getAskAmount(), "order res");
//            }
//        };
//        t3.start();
//        t1.start();
//
//
//        t3.join();
//        t1.join();

        // p2->p1 正链
        // p2 下买单
        // 同时 p3 下卖单 p1 下卖单


        //p1 生成订单号
        //关注交易成功  或 账号 余额是否够用 如果够用启动交易

    }


    @Test
    public void testCurrency(){
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

        log.info("posCyclePrice:{}, negCyclePrice:{}", triangleCurrency.posCyclePrice(), triangleCurrency.negCyclePrice());

    }


    @Test
    public void testDataCurrency(){
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

        log.info("posCyclePrice:{}, negCyclePrice:{}", triangleCurrency.posCyclePrice(), triangleCurrency.negCyclePrice());

    }


} 
