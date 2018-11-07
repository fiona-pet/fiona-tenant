package com.fionapet.tenant.xchange;

import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.entity.TriangleCurrency;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bitstamp.BitstampAuthenticatedV2;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.bitstamp.dto.account.BitstampBalance;
import org.knowm.xchange.bitstamp.dto.trade.BitstampOrder;
import org.knowm.xchange.bitstamp.service.BitstampAccountServiceRaw;
import org.knowm.xchange.bitstamp.service.BitstampTradeServiceRaw;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class XchangeService {

    /**
     * 订单 信息
     *
     * @param currencyPair eg: BTC_USD
     */
    public TopOneOrderBook getOrderBookByCurrencyPair(String instanceName,
                                                      CurrencyPair currencyPair)
            throws Exception {
        OrderBook orderBook = getOrderBook(instanceName, currencyPair);

        TopOneOrderBook topOneOrderBook = new TopOneOrderBook();

        topOneOrderBook.setCurrencyPair(currencyPair.toString());

        topOneOrderBook.setBidPrice(orderBook.getBids().get(0).getLimitPrice().floatValue());
        topOneOrderBook.setAskPrice(orderBook.getAsks().get(0).getLimitPrice().floatValue());

        return topOneOrderBook;
    }

    public OrderBook getOrderBook(String instanceName, CurrencyPair currencyPair){
        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(instanceName);

        MarketDataService marketDataService = exchange.getMarketDataService();

        OrderBook orderBook = null;
        try {
            orderBook = marketDataService.getOrderBook(currencyPair);
        } catch (Exception e) {
            log.debug("getOrderBook error!", e.getMessage());
        }

        return orderBook;
    }

    /**
     * 获取 市场 币对列表
     */
    public List<CurrencyPair> getExchangeSymbols(String instanceName) {
        try {
            Exchange exchange = ExchangeFactory.INSTANCE.createExchange(instanceName);
            return exchange.getExchangeSymbols();
        } catch (Exception e) {
            log.warn("getExchangeSymbols", e);
        }
        return new ArrayList<>();
    }

    private Exchange create(String instanceName) {
        ExchangeSpecification exSpec = new BitstampExchange().getDefaultExchangeSpecification();
        exSpec.setUserName("rkrh7895");
        exSpec.setApiKey("4PY9FR49f0IZA8sRSVk12ygLDMj0Ensq");
        exSpec.setSecretKey("H78k4khdm7cXQwKz7gHjIwpWs9bXLu9P");

        return ExchangeFactory.INSTANCE.createExchange(exSpec);
    }

    /**
     * 购买
     *
     * @param instanceName   市场
     * @param originalAmount 购买数量
     * @param currencyPair   币对
     * @param limitPrice     挂单价格
     * @return 挂单信息
     */
    public LimitOrder buy(String instanceName, BigDecimal originalAmount,
                          CurrencyPair currencyPair, BigDecimal limitPrice) {
        try {
            Exchange exchange = create(instanceName);

            TradeService tradeService = exchange.getTradeService();

            log.info("account:{}", exchange.getAccountService().getAccountInfo());

            // place a limit buy order
            LimitOrder limitOrder =
                    new LimitOrder(
                            (Order.OrderType.BID),
                            originalAmount,
                            currencyPair,
                            null,
                            null,
                            limitPrice);

            String limitOrderReturnValue = tradeService.placeLimitOrder(limitOrder);

            log.info("Limit Order return value:{}, object:{}", limitOrderReturnValue, limitOrder);

            return limitOrder;
        } catch (Exception e) {
            log.warn("LimitOrder buy", e);
        }
        return null;
    }


    public BitstampOrder sell(String instanceName, BigDecimal originalAmount,
                              CurrencyPair currencyPair, BigDecimal limitPrice) {

        try {
            Exchange exchange = create(instanceName);

            BitstampTradeServiceRaw tradeService =
                    (BitstampTradeServiceRaw) exchange.getTradeService();

            log.info("account:{}", exchange.getAccountService().getAccountInfo());

            // place a limit sell order
            BitstampOrder order =
                    tradeService.placeBitstampOrder(
                            currencyPair, BitstampAuthenticatedV2.Side.sell, originalAmount,
                            limitPrice);

            log.info("BitstampOrder Order return value:{}", order);

            return order;
        } catch (Exception e) {
            log.warn("BitstampOrder sell", e);
        }
        return null;
    }

    /**
     * 获取订单
     */
    public OpenOrders getOpenOrders(String instanceName)
            throws IOException {

        Exchange exchange = create(instanceName);

        TradeService tradeService = exchange.getTradeService();

        OpenOrdersParams openOrdersParamsAll = tradeService.createOpenOrdersParams();

        OpenOrders openOrders = tradeService.getOpenOrders(openOrdersParamsAll);

        log.debug("Trade Open Orders for {}: {}", openOrders);

        return openOrders;
    }


    /**
     * 获取 币对组合
     */
    public Set<TriangleCurrency> grenCurrencyPair(final Currency mid,
                                                   final List<CurrencyPair> currencyPairs) {
        //找到 所有以 base 为定价币的 币对
        List<CurrencyPair> hasBase = currencyPairs.stream().filter(new Predicate<CurrencyPair>() {
            @Override
            public boolean test(CurrencyPair currencyPair) {
                return currencyPair.counter.equals(mid);
            }
        }).collect(Collectors.toList());

        log.debug("hasBase:{}", hasBase);

        Set<TriangleCurrency> triangleCurrencies = new HashSet<>();

        for (int i = 0; i < hasBase.size(); i++) {
            for (int j = i + 1; j < hasBase.size(); j++) {
                TriangleCurrency triangleCurrency = new TriangleCurrency();

                CurrencyPair p1 = hasBase.get(i);
                CurrencyPair p2 = hasBase.get(j);

                triangleCurrency.setBaseCur(p1.base);
                triangleCurrency.setQuoteCur(p2.base);
                triangleCurrency.setMidCur(mid);

                if (hasCurrencyPair(triangleCurrency, currencyPairs)) {
                    triangleCurrencies.add(triangleCurrency);
                }

                TriangleCurrency triangleCurrency2 = new TriangleCurrency();
                triangleCurrency2.setBaseCur(p2.base);
                triangleCurrency2.setQuoteCur(p1.base);
                triangleCurrency2.setMidCur(mid);

                if (hasCurrencyPair(triangleCurrency2, currencyPairs)) {
                    triangleCurrencies.add(triangleCurrency2);
                }
            }
        }

        return triangleCurrencies;
    }

    private boolean hasCurrencyPair(final TriangleCurrency triangleCurrency, List<CurrencyPair> currencyPairs) {
        List<CurrencyPair> hasBase = currencyPairs.stream().filter(new Predicate<CurrencyPair>() {
            @Override
            public boolean test(CurrencyPair currencyPair) {
                return currencyPair.base == triangleCurrency.getBaseCur();
            }
        }).collect(Collectors.toList());

        List<CurrencyPair> hasQuote = hasBase.stream().filter(new Predicate<CurrencyPair>() {
            @Override
            public boolean test(CurrencyPair currencyPair) {
                return currencyPair.counter == triangleCurrency.getQuoteCur();
            }
        }).collect(Collectors.toList());


        return hasQuote.size() == 1;
    }

    public BitstampBalance.Balance getBalance(String instanceName, final String currency) {
        Exchange exchange = create(instanceName);

        BitstampAccountServiceRaw accountService = (BitstampAccountServiceRaw)exchange.getAccountService();

        try {

            Collection<BitstampBalance.Balance> balances = accountService.getBitstampBalance().getBalances();
            return balances.stream().filter(balance -> currency.toLowerCase().equals(balance.getCurrency())).findFirst().get();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
