package com.fionapet.tenant.tc.entity;

import com.fionapet.tenant.security.audit.AbstractAuditableEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;

import java.math.BigDecimal;

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
    private BigDecimal bid = new BigDecimal("0");
    private BigDecimal bidAmount = new BigDecimal("0");
    private BigDecimal ask = new BigDecimal("0");
    private BigDecimal askAmount = new BigDecimal("0");
    private String currencyPair;
}
