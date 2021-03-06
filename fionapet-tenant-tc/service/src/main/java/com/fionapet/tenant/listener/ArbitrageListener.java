package com.fionapet.tenant.listener;//package com.fionapet.tenant.listener;
//
//import com.fionapet.tenant.tc.entity.Arbitrage;
//import com.fionapet.tenant.tc.entity.ArbitrageLog;
//import com.fionapet.tenant.tc.entity.Exchange;
//import com.fionapet.tenant.tc.entity.TopOneOrderBook;
//import com.fionapet.tenant.tc.entity.TrianglePair;
//import com.fionapet.tenant.tc.service.ArbitrageLogService;
//import com.fionapet.tenant.tc.service.OrderBookPriceService;
//import com.fionapet.tenant.tc.service.TopOneOrderBookService;
//import com.fionapet.tenant.tc.service.TrianglePairCacheService;
//import com.fionapet.tenant.xchange.XchangeService;
//import com.google.common.collect.Sets;
//import lombok.extern.slf4j.Slf4j;
//import org.knowm.xchange.currency.CurrencyPair;
//import org.knowm.xchange.dto.marketdata.OrderBook;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.event.EventListener;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//@Slf4j
//@Component
//@EnableAsync
//public class ArbitrageListener {
//
//    @Autowired
//    XchangeService xchangeService;
//
//    @Autowired
//    TopOneOrderBookService topOneOrderBookService;
//
//    @Autowired
//    ArbitrageLogService arbitrageLogService;
//
//    @Autowired
//    OrderBookPriceService orderBookPriceService;
//
//    @Autowired
//    TrianglePairCacheService trianglePairCacheService;
//
//    @Autowired
//    ApplicationContext applicationContext;
//
//    @EventListener
//    @Async
//    public void arbitrage(ArbitrageEvent arbitrageEvent) {
//        Arbitrage arbitrage = null;
//        TrianglePair trianglePair = arbitrageEvent.getTrianglePair();
//
//        try {
//            arbitrage = trianglePair.arbitrage();
//
//            if (null != arbitrage) {
//                PlaceOrderEvent placeOrderEvent = new PlaceOrderEvent(this, arbitrage.getType(), arbitrage.getQuoteMidOrderBookPrice(),
//                                                                      arbitrage.getBaseQuoteOrderBookPrice(),
//                                                                      arbitrage.getBaseMidOrderBookPrice());
//                applicationContext.publishEvent(placeOrderEvent);
//                ArbitrageLog arbitrageLog = new ArbitrageLog(trianglePair);
//                arbitrageLog.setArbitrage(arbitrage.getArbitrage());
//                arbitrageLog.setPecentage(arbitrage.getPecentage());
//                arbitrageLog.setType(arbitrage.getType());
//                arbitrageLog.setExchangeId(1l);
//
//                arbitrageLogService.save(arbitrageLog);
//
//                if (arbitrage.getArbitrage() > 0) {
//                    trianglePair.getMarketPrice().setArbitrageLogId(arbitrageLog.getId());
//                    trianglePair.getBaseMidPrice().setArbitrageLogId(arbitrageLog.getId());
//                    trianglePair.getQuoteMidPrice().setArbitrageLogId(arbitrageLog.getId());
//
//                    orderBookPriceService.save(trianglePair.getMarketPrice());
//                    orderBookPriceService.save(trianglePair.getBaseMidPrice());
//                    orderBookPriceService.save(trianglePair.getQuoteMidPrice());
//                }
//            }
//        } finally {
//            trianglePairCacheService.delete(trianglePair.getKey());
//
//        }
//    }
//
//}
