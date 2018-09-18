package com.fionapet.tenant.tc.service;

import com.fionapet.tenant.tc.entity.ArbitrageLog;
import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.repository.ArbitrageLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArbitrageLogService {

    private static final Logger log = LoggerFactory.getLogger(ArbitrageLogService.class);

    @Autowired
    private ArbitrageLogRepository arbitrageLogRepository;

    @Transactional
    public ArbitrageLog save(ArbitrageLog entity) {
        log.debug("Saving ArbitrageLog {}...", entity);
        entity = arbitrageLogRepository.save(entity);
        log.debug("ArbitrageLog saved {}!", entity);
        return entity;
    }

    public void clean(Long exchangeId) {
        arbitrageLogRepository.deleteByExchangeIdAndArbitrageLessThan(exchangeId, 0);
    }
}
