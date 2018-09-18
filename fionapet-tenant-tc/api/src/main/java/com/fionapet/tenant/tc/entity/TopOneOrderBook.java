package com.fionapet.tenant.tc.entity;


import com.fionapet.tenant.security.audit.AbstractAuditableEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "top_one_order_book")
@Getter
@Setter
@ToString
public class TopOneOrderBook extends AbstractAuditableEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    /**
     * 市场id
     */
    private Long exchangeId;

    /**
     * 币对
     */
    private String currencyPair;

    /**
     * 买 1
     */
    private float bidPrice;

    /**
     * 卖 1
     */
    private float askPrice;

    /**
     * 剩余订货量
     */
    private float bidRemainingAmount;

    /**
     * 剩余订货量
     */
    private float askRemainingAmount;


}
