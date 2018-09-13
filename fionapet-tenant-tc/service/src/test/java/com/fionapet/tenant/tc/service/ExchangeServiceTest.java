package com.fionapet.tenant.tc.service;

import com.fionapet.tenant.multitenant.TenantContextHolder;
import com.fionapet.tenant.tc.entity.Exchange;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * ExchangeService Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>九月 13, 2018</pre>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ExchangeServiceTest {
    @Autowired
    private ExchangeService exchangeService;

    @Before
    public void before() throws Exception {
        TenantContextHolder.setCurrentSchema("user1");
    }

    @After
    public void after() throws Exception {

    }

    /**
     * Method: save(Exchange demo)
     */
    @Test
    public void testSave() throws Exception {
        Exchange exchange = new Exchange();
        exchange.setName("bitstamp");
        exchange.setWebsite("https://www.bitstamp.net");
        exchange.setLocation("英国");
        exchange.setInstanceName("org.knowm.xchange.bitstamp.BitstampExchange");
        exchangeService.save(exchange);
    }




} 
