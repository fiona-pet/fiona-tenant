package com.fionapet.tenant.xchange;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

@Slf4j
public abstract class StopWatchDo<DO, R> {
    public R  watchDo(String tag, DO handler){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(tag);
        R r =  this.doing(handler);
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());
        return r;
    }

    abstract R doing(DO handler);
}
