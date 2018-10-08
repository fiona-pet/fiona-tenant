package com.fionapet.tenant.tc.service;

import com.fionapet.tenant.tc.entity.ArbitrageLog;
import com.fionapet.tenant.tc.entity.OrderBookPrice;
import com.fionapet.tenant.tc.repository.ArbitrageLogRepository;
import com.fionapet.tenant.tc.repository.OrderBookPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderBookPriceService {

    private static final Logger log = LoggerFactory.getLogger(OrderBookPriceService.class);

    @Autowired
    private OrderBookPriceRepository orderBookPriceRepository;

    @Transactional
    public OrderBookPrice save(OrderBookPrice entity) {
        entity = orderBookPriceRepository.save(entity);
        return entity;
    }
}
