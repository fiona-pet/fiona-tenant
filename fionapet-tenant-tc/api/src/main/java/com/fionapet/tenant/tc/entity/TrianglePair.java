package com.fionapet.tenant.tc.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.knowm.xchange.currency.CurrencyPair;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Getter
@Setter
@ToString
public class TrianglePair {

	private static final long serialVersionUID = 1L;

    /**
     * 中专币对
     */
	private CurrencyPair convertPair;

    /**
     * p2 基础币 转 中间币对
     */
	private CurrencyPair fromBasePair;

	/**
	 * p1 中间币 转 基础币
	 */
	private CurrencyPair toBasePair;
}
