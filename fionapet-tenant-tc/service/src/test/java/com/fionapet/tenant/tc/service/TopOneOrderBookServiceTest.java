package com.fionapet.tenant.tc.service;

import com.fionapet.tenant.multitenant.TenantContextHolder;
import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * TopOneOrderBookService Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>九月 12, 2018</pre>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Setter
@Slf4j
public class TopOneOrderBookServiceTest {
    @Autowired
    TopOneOrderBookService topOneOrderBookService;

    @Autowired
    TenantContextHolder tenantContextHolder;

    @Before
    public void before() throws Exception {
        tenantContextHolder.setCurrentSchema("user1");
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: save(TopOneOrderBook demo)
     */
    @Test
    public void testSave() throws Exception {
        TopOneOrderBook topOneOrderBook = new TopOneOrderBook();
        topOneOrderBookService.save(topOneOrderBook);
    }

    /**
     * Method: delete(TopOneOrderBook demo)
     */
    @Test
    public void testDelete() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: findById(Long id)
     */
    @Test
    public void testFindById() throws Exception {
//TODO: Test goes here... 
    }


} 
