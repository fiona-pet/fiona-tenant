package com.fionapet.tenant.tc.service;

import com.fionapet.tenant.tc.entity.Exchange;
import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.repository.ExchangeRepository;
import com.fionapet.tenant.tc.repository.TopOneOrderBookRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ExchangeService {

    @Autowired
    private ExchangeRepository repository;

    @Transactional
    public Exchange save(Exchange demo) {
        log.debug("Saving Exchange {}...", demo);
        demo = repository.save(demo);
        log.debug("Exchange saved {}!", demo);
        return demo;
    }

    public List<Exchange> list(){
        return repository.findAllBy();
    }
}
