package com.fionapet.tenant.tc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Arbitrage {

    /**
     * 利润
     */
    private float arbitrage;
    /**
     * 百分比
     */
    private float pecentage;
}
