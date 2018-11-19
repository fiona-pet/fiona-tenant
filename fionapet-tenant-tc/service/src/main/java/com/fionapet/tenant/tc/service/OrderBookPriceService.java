package com.fionapet.tenant.tc.service;

import com.fionapet.tenant.tc.entity.OrderBookPrice;
import com.fionapet.tenant.tc.entity.TriangleCurrency;
import com.fionapet.tenant.tc.repository.OrderBookPriceRepository;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Predicate;

@Service
public class OrderBookPriceService {
    private static final Logger log = LoggerFactory.getLogger(OrderBookPriceService.class);
    private static final Currency MID = Currency.USD;

    @Autowired
    private OrderBookPriceRepository orderBookPriceRepository;

    @Transactional
    public OrderBookPrice save(OrderBookPrice entity) {
        entity = orderBookPriceRepository.save(entity);
        return entity;
    }

    public TriangleCurrency getByArbitrageLogId(Long arbitrageLogId) {
        List<OrderBookPrice> orderBookPrices = orderBookPriceRepository.findByArbitrageLogId(arbitrageLogId);
        OrderBookPrice bq = orderBookPrices.stream().findFirst().filter(
                orderBookPrice -> orderBookPrice.getCurrencyPair().indexOf(MID.toString())==-1).get();

        CurrencyPair bqCurrencyPair = new CurrencyPair(bq.getCurrencyPair());

        CurrencyPair bmCurrencyPair = new CurrencyPair(bqCurrencyPair.base, MID);

        OrderBookPrice bm = orderBookPrices.stream().filter(
                orderBookPrice -> {
                    log.info("getCurrencyPair():{}, bm:{}", orderBookPrice.getCurrencyPair(), bmCurrencyPair.toString());
                    return orderBookPrice.getCurrencyPair().equals(bmCurrencyPair.toString());
                }).findFirst().get();

        CurrencyPair qmCurrencyPair = new CurrencyPair(bqCurrencyPair.counter, MID);

        OrderBookPrice qm = orderBookPrices.stream().filter(
                orderBookPrice -> orderBookPrice.getCurrencyPair().equals(qmCurrencyPair.toString())).findFirst().get();

        TriangleCurrency triangleCurrency = new TriangleCurrency();
        triangleCurrency.setBaseQuoteOrderBookPrice(bq);
        triangleCurrency.setBaseMidOrderBookPrice(bm);
        triangleCurrency.setQuoteMidOrderBookPrice(qm);

        return triangleCurrency;
    }
}
