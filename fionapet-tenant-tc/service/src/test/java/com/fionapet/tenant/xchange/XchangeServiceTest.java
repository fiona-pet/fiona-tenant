package com.fionapet.tenant.xchange;

import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.entity.TrianglePair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import com.fionapet.tenant.multitenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

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

    String instanceName = "org.knowm.xchange.binance.BinanceExchange";

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

        List<TrianglePair> trianglePairs = xchangeService.grenCurrencyPair(Currency.USDT, currencyPairList);

        log.debug("trianglePairs:{}" , trianglePairs);
    }

} 
