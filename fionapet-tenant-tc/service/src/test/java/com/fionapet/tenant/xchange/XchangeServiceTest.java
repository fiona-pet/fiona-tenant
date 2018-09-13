package com.fionapet.tenant.xchange;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import com.fionapet.tenant.multitenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
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
        OrderBook orderBook = xchangeService.getOrderBookByCurrencyPair(instanceName,
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

        Assert.assertTrue(currencyPairList.size()>0);
    }

} 
