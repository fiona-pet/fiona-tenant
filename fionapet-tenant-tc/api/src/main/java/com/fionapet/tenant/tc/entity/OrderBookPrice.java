package com.fionapet.tenant.tc.entity;

import com.fionapet.tenant.security.audit.AbstractAuditableEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "order_book_price")
@Setter
@Getter
public class OrderBookPrice extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private Long arbitrageLogId;
    private float buy;
    private float buyAmount;
    private float sell;
    private float sellAmount;
    private String currencyPair;

    public OrderBookPrice(OrderBook orderBook, CurrencyPair type) {
        this.buy = orderBook.getBids().get(0).getLimitPrice().floatValue();
        this.buyAmount = orderBook.getBids().get(0).getRemainingAmount().floatValue();
        this.sell = orderBook.getAsks().get(0).getLimitPrice().floatValue();
        this.sellAmount = orderBook.getAsks().get(0).getRemainingAmount().floatValue();
        this.currencyPair = type.toString();
    }

    public OrderBookPrice() {
    }
}
