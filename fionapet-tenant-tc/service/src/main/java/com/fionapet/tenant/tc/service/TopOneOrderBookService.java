package com.fionapet.tenant.tc.service;

import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.entity.TrianglePair;
import com.fionapet.tenant.tc.repository.TopOneOrderBookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TopOneOrderBookService {

    private static final Logger log = LoggerFactory.getLogger(TopOneOrderBookService.class);

    @Autowired
    private TopOneOrderBookRepository topOneOrderBookRepository;

    @Transactional
    public TopOneOrderBook save(TopOneOrderBook demo) {
        log.debug("Saving TopOneOrderBook {}...", demo);
        demo = topOneOrderBookRepository.save(demo);
        log.debug("TopOneOrderBook saved {}!", demo);
        return demo;
    }

    @Transactional
    public void delete(TopOneOrderBook demo) {
        log.debug("Deleting TopOneOrderBook {}...", demo);
        topOneOrderBookRepository.delete(demo);
        log.debug("Deleted TopOneOrderBook {}", demo);
    }

    public Optional<TopOneOrderBook> findById(Long id) {
        log.debug("Finding TopOneOrderBook by id {}...", id);
        Optional<TopOneOrderBook> demo = topOneOrderBookRepository.findById(id);
        log.debug("Found TopOneOrderBook {}!", demo);
        return demo;
    }

    public TopOneOrderBook findByExchangeIdAndCurrencyPair(Long exchangeId, String currencyPair) {
        return topOneOrderBookRepository.findByExchangeIdAndCurrencyPair(exchangeId, currencyPair);
    }

    @Transactional
    public void save(List<TopOneOrderBook> topOneOrderBooks) {
        topOneOrderBookRepository.saveAll(topOneOrderBooks);
    }
}
