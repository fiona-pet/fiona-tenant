package com.fionapet.tenant.tc.service;

import com.fionapet.tenant.tc.entity.TrianglePair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

@Service
@EnableCaching
@Slf4j
public class TrianglePairCacheService {
    private static final String DEMO_CACHE_NAME = "demo";
    private static final String CACHE_KEY = "'trianglepair'";

    @CachePut(value = DEMO_CACHE_NAME, key = "'trianglepair:'+#trianglePair.getKey()")
    public void putTrianglePair(TrianglePair trianglePair) {
        log.info("add cache:{}", trianglePair.getKey());
    }


    @Cacheable(value = DEMO_CACHE_NAME, key = "'trianglepair:'+#key")
    public TrianglePair getTrianglePair(String key) {
        log.info("read cache:{}", key);
        return null;
    }

    @CacheEvict(value = DEMO_CACHE_NAME, key = "'trianglepair:'+#key")//这是清除缓存
    public void delete(String key) {
        log.info("remove 缓存 数据:{}", key);
    }


}
