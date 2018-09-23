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
     * 中间 - 持有 usd
     */
    private String midCur;

    /**
     * 基础 ltc
     */
    private String baseCur;

    /**
     * 定价币 btc
     */
    private String quoteCur;

    /**
     * 套利
     */
    private Float arbitrage;

    /**
     * 百分比
     */
    private Float pecentage;

    /**
     * 套利类型
     */
    private String type;

    public ArbitrageLog(TrianglePair trianglePair) {
        this.midCur = trianglePair.getMidCur().toString();
        this.quoteCur = trianglePair.getQuoteCur().toString();
        this.baseCur = trianglePair.getBaseCur().toString();
    }

    public ArbitrageLog() {
    }

}
