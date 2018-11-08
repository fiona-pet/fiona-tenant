package com.fionapet.tenant.tc.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.Objects;

@Data
@Getter
@Setter
@Slf4j
@ToString
public class TriangleCurrency {
    public static final float base_quote_slippage = 0.00f; //# 设定市场价滑点百分比
    public static final float base_mid_slippage = 0.00f;
    public static final float quote_mid_slippage = 0.00f;

    public static final float base_quote_fee = 0.0025f;  //# 设定手续费比例
    public static final float base_mid_fee = 0.0025f;
    public static final float quote_mid_fee = 0.0025f;

    /**
     * 定价资产
     */
    private Currency quoteCur;

    /**
     * 中间资产
     */
    private Currency midCur;

    /**
     * 基准资产
     */
    private Currency baseCur;

    /**
     * p2
     */
    private OrderBookPrice quoteMidOrderBookPrice;
    /**
     * p3
     */
    private OrderBookPrice baseQuoteOrderBookPrice;
    /**
     * p1
     */
    private OrderBookPrice baseMidOrderBookPrice;

    /**
     * p3
     *
     * @return
     */
    public CurrencyPair getBaseQuotePair() {
        return new CurrencyPair(this.getBaseCur(), this.getQuoteCur());
    }

    /**
     * p1
     *
     * @return
     */
    public CurrencyPair getBaseMidPair() {
        return new CurrencyPair(this.getBaseCur(), this.getMidCur());
    }

    /**
     * p2
     *
     * @return
     */
    public CurrencyPair getQuoteMidPair() {
        return new CurrencyPair(this.getQuoteCur(), this.getMidCur());
    }

    public float posCyclePrice(){
        float posCycleFairPrice = posCycleFairPrice();
        float posCyclePrice = posCycleFairPrice- sumSlippageFee();
        log.info("posCycleFairPrice:{}, posCyclePrice:{}", posCycleFairPrice, posCyclePrice);
        return posCyclePrice;
    }

    public float negCyclePrice(){
        float negCycleFairPrice = negCycleFairPrice();
        float negCyclePrice = negCycleFairPrice-sumSlippageFee();
        log.info("negCycleFairPrice:{}, negCyclePrice:{}", negCyclePrice, negCyclePrice);
        return negCyclePrice;
    }

    /**
     * 正循环公允价和市场价的价差比例 (base_mid_price_buy_1 / quote_mid_price_sell_1 -
     * market_price_sell_1)/market_price_sell_1
     */
    private float posCycleFairPrice() {

        float baseMidPriceBid1 = this.getBaseMidOrderBookPrice().getBid().floatValue();
        float quoteMidPriceAsk1 = this.getQuoteMidOrderBookPrice().getAsk().floatValue();
        float marketPriceAsk1 = this.getBaseQuoteOrderBookPrice().getAsk().floatValue();

        return (baseMidPriceBid1 / quoteMidPriceAsk1 - marketPriceAsk1) / marketPriceAsk1;
    }

    /**
     * 逆循环公允价和市场价的价差比例 (market_price_buy_1 - base_mid_price_sell_1 / quote_mid_price_buy_1)/market_price_buy_1
     */
    private float negCycleFairPrice() {

        float marketPriceBid1 = this.getBaseQuoteOrderBookPrice().getBid().floatValue();
        float baseMidPriceAsk1 = this.getBaseMidOrderBookPrice().getAsk().floatValue();
        float quoteMidPriceBid1 = this.getQuoteMidOrderBookPrice().getBid().floatValue();

        return (marketPriceBid1 - baseMidPriceAsk1 / quoteMidPriceBid1) / marketPriceBid1;
    }

    /**
     * 所有市场的费率总和再加上滑点总和
     */
    private float sumSlippageFee() {
        return base_quote_slippage + base_mid_slippage + quote_mid_slippage +
                base_quote_fee + base_mid_fee + quote_mid_fee;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TriangleCurrency that = (TriangleCurrency) o;
        return Objects.equals(quoteCur, that.quoteCur) &&
                Objects.equals(midCur, that.midCur) &&
                Objects.equals(baseCur, that.baseCur);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quoteCur, midCur, baseCur);
    }
}
