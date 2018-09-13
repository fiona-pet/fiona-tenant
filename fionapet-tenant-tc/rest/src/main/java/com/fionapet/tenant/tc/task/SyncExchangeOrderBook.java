package com.fionapet.tenant.tc.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@EnableScheduling
@Component
@Slf4j
public class SyncExchangeOrderBook {


    @Scheduled(cron = "0/5 * * * * ?") //每5秒执行一次
    public void doSomething() throws Exception {
        log.info("每2秒执行一个的定时任务："+new Date());
    }
}
