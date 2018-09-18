package com.fionapet.tenant.tc.entity;


import com.fionapet.tenant.security.audit.AbstractAuditableEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.knowm.xchange.currency.CurrencyPair;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "arbitrage_log")
@Getter
@Setter
@ToString
public class ArbitrageLog extends AbstractAuditableEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    /**
     * 市场id
     */
    private Long exchangeId;

    /**
     * 中专币对
     */
    private String convertPair;

    /**
     * p2 基础币 转 中间币对
     */
    private String fromBasePair;

    /**
     * p1 中间币 转 基础币
     */
    private String toBasePair;

    private float convertPairSellPrice;
    private float fromBasePairSellPrice;
    private float toBasePairBuyPrice;

    private float arbitrage;

    public ArbitrageLog(TrianglePair trianglePair) {
        this.convertPair = trianglePair.getConvertPair().toString();
        this.fromBasePair = trianglePair.getFromBasePair().toString();
        this.toBasePair = trianglePair.getToBasePair().toString();
        this.convertPairSellPrice = trianglePair.getConvertPairSellPrice();
        this.fromBasePairSellPrice = trianglePair.getFromBasePairSellPrice();
        this.toBasePairBuyPrice = trianglePair.getToBasePairBuyPrice();
    }

    public ArbitrageLog() {
    }
}
