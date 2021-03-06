package com.fionapet.tenant.tc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Arbitrage {

    public static final String TYPE_NEG = "NEG";
    public static final String TYPE_POS = "POS";

    /**
     * p1
     */
    private OrderBookPrice quoteMidOrderBookPrice;
    /**
     * p2
     */
    private OrderBookPrice baseQuoteOrderBookPrice;
    /**
     * p3
     */
    private OrderBookPrice baseMidOrderBookPrice;

    /**
     * 利润
     */
    private float arbitrage;
    /**
     * 百分比
     */
    private float pecentage;

    private String type;
}
