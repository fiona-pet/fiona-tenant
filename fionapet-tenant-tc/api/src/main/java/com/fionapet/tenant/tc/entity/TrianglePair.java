package com.fionapet.tenant.tc.entity;


import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;

import java.util.Collections;
import java.util.Objects;

@Data
@Getter
@Setter
@Slf4j
/**
 * '''
 *                 三角套利的基本思路是，用两个市场（比如BTC/CNY，LTC/CNY）的价格（分别记为P1，P2），
 *                 计算出一个公允的LTC/BTC价格（P2/P1），如果该公允价格跟实际的LTC/BTC市场价格（记为P3）不一致，
 *                 就产生了套利机会
 *
 *                 对应的套利条件就是：
 *                 ltc_cny_buy_1_price >
 *                 btc_cny_sell_1_price*ltc_btc_sell_1_price*(1+btc_cny_slippage)*(1+ltc_btc_slippage)
 *                 /[(1-btc_cny_fee)*(1-ltc_btc_fee)*(1-ltc_cny_fee)*(1-ltc_cny_slippage)]
 *                 考虑到各市场费率都在千分之几的水平，做精度取舍后，该不等式可以进一步化简成：
 *                 (ltc_cny_buy_1_price/btc_cny_sell_1_price-ltc_btc_sell_1_price)/ltc_btc_sell_1_price
 *                 >btc_cny_slippage+ltc_btc_slippage+ltc_cny_slippage+btc_cny_fee+ltc_cny_fee+ltc_btc_fee
 *                 基本意思就是：只有当公允价和市场价的价差比例大于所有市场的费率总和再加上滑点总和时，做三角套利才是盈利的。
 *             '''
 */
public class TrianglePair {

    private static final long serialVersionUID = 1L;

    public static final float base_quote_slippage = 0.00f; //# 设定市场价滑点百分比
    public static final float base_mid_slippage = 0.00f;
    public static final float quote_mid_slippage = 0.00f;

    public static final float base_quote_fee = 0.0025f;  //# 设定手续费比例
    public static final float base_mid_fee = 0.0025f;
    public static final float quote_mid_fee = 0.0025f;

    public static final float order_ratio_base_quote = 1f;//  # 设定吃单比例
    public static final float order_ratio_base_mid = 1f;


    //# 设定账户最少预留数量,根据你自己的初始市场情况而定, 注意： 是数量而不是比例
    public static final float base_quote_quote_reserve = 0.0f;
    public static final float base_quote_base_reserve = 0.0f;
    public static final float quote_mid_mid_reserve = 0.0f;
    public static final float quote_mid_quote_reserve = 0.0f;
    public static final float base_mid_base_reserve = 0.0f;
    public static final float base_mid_mid_reserve = 0.0f;

    // # 最小的交易单位设定
    public static final float min_trade_unit = 0.02f;   // # LTC/BTC交易对，设置为0.2, ETH/BTC交易对，设置为0.02

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


    public String getKey(){
        return quoteCur.getCurrencyCode() + "-" + midCur.getCurrencyCode() + "-" + baseCur.getCurrencyCode();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TrianglePair that = (TrianglePair) o;
        return Objects.equals(quoteCur, that.quoteCur) &&
               Objects.equals(midCur, that.midCur) &&
               Objects.equals(baseCur, that.baseCur);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quoteCur, midCur, baseCur);
    }

    /**
     * base_cur/quote_cur p3
     */
    @ToStringExclude
    OrderBook marketPriceOrderBook;
    /**
     * base_cur/mid_cur p2
     */
    @ToStringExclude
    OrderBook baseMidPriceOrderBook;
    /**
     * quote_cur/mid_cur p1
     */
    @ToStringExclude
    OrderBook quoteMidPriceOrderBook;

    private OrderBookPrice marketPrice;
    private OrderBookPrice baseMidPrice;
    private OrderBookPrice quoteMidPrice;


    public void setMarketPriceOrderBook(OrderBook marketPriceOrderBook) {
        this.marketPriceOrderBook = marketPriceOrderBook;
        this.marketPrice = new OrderBookPrice(this.marketPriceOrderBook, new CurrencyPair(this.baseCur, this.quoteCur));
    }

    public void setBaseMidPriceOrderBook(OrderBook baseMidPriceOrderBook) {
        this.baseMidPriceOrderBook = baseMidPriceOrderBook;
        this.baseMidPrice = new OrderBookPrice(this.baseMidPriceOrderBook, new CurrencyPair(this.baseCur, this.midCur));
    }

    public void setQuoteMidPriceOrderBook(OrderBook quoteMidPriceOrderBook) {
        this.quoteMidPriceOrderBook = quoteMidPriceOrderBook;
        this.quoteMidPrice = new OrderBookPrice(this.quoteMidPriceOrderBook, new CurrencyPair(this.quoteCur, this.midCur));
    }

    public Arbitrage posArbitrage() {
        float marketBuySize = getMarketBuySize();

        return this.posCycle(marketBuySize);
    }

    public Arbitrage negArbitrage() {
        float marketSellSize = getMarketSellSize();

        return this.negCycle(marketSellSize);
    }

    public Arbitrage arbitrage() {
        float posCycleFairPrice = posCycleFairPrice();
        float negCycleFairPrice = negCycleFairPrice();

        if (posCycleFairPrice > this.sumSlippageFee()) {
            float marketBuySize = getMarketBuySize();

            //TODO market_buy_size = downRound(market_buy_size, 2)

            return this.posCycle(marketBuySize);

        } else if (negCycleFairPrice > this.sumSlippageFee()) {
            float marketSellSize = getMarketSellSize();

            return this.negCycle(marketSellSize);
        }

        return null;
    }

    /**
     * ''' 逆循环套利 逆循环套利的顺序如下： 先去LTC/BTC吃单卖出LTC，买入BTC，然后根据LTC/BTC的成交量，使用多线程， 同时在LTC/CNY和BTC/CNY市场进行对冲。
     * LTC/CNY市场吃单买入LTC，BTC/CNY市场吃单卖出BTC。
     *
     * '''
     */
    private Arbitrage negCycle(float marketSellSize) {
        float marketPriceBuy1 = this.marketPrice.getBuy();
        float baseMidPriceSell1 = this.baseMidPrice.getSell();
        float quoteMidPriceBuy1 = this.quoteMidPrice.getBuy();

        // base/quote 卖出 base 得到 quote
        float p3 = marketPriceBuy1 * (1 + base_quote_slippage);

        // quote/mid 卖出 quote 得到 mid
        float p1 = quoteMidPriceBuy1 * (1 + quote_mid_slippage);

        // base/mid 买入 base 花费 mid
        float p2 = baseMidPriceSell1 * (1 + base_mid_slippage);

        // 卖出 marketSellSize 个 base 得到 quote 的数量 p3
        float quoteSellSize = marketSellSize * p3 * (1 - base_quote_fee);

        // 卖出 quoteSellSize 个 quote 得到 mid 的数量 p2
        float midSizeFormSellQuote = quoteSellSize * p1 * (1 - quote_mid_fee);

        // 买入 marketSellSize 个 base 花费 mid 的数量 p1
        float midSizeForBuyBase = marketSellSize * p2 / (1 - base_mid_fee);

        float arbitrageValue = midSizeFormSellQuote - midSizeForBuyBase;

        float arbitragePecentage = arbitrageValue / midSizeForBuyBase;

        Arbitrage arbitrage = new Arbitrage();
        arbitrage.setArbitrage(arbitrageValue);
        arbitrage.setPecentage(arbitragePecentage);
        arbitrage.setType(Arbitrage.TYPE_NEG);

        return arbitrage;
    }

    /**
     * ''' 卖出的下单保险数量计算 假设BTC/CNY盘口流动性好 1. LTC/BTC买方盘口吃单数量：ltc_btc_buy1_quantity*order_ratio_ltc_btc，其中ltc_btc_buy1_quantity
     * 代表LTC/BTC买一档的数量， order_ratio_ltc_btc代表本策略在LTC/BTC盘口的吃单比例 2. LTC/CNY卖方盘口卖单数量：ltc_cny_sell1_quantity*order_ratio_ltc_cny，其中order_ratio_ltc_cny代表本策略在LTC/CNY盘口的吃单比例
     * 3. LTC/BTC账户中可以用来卖LTC的数量： ltc_available - ltc_reserve， 其中，ltc_available表示该账户中可用的LTC数量，ltc_reserve表示该账户中应该最少预留的LTC数量
     * （这个数值由用户根据自己的风险偏好来设置，越高代表用户风险偏好越低）。 4.	BTC/CNY账户中可以用来卖BTC的BTC额度和对应的LTC个数： btc_available -
     * btc_reserve, 可以置换成 (btc_available-btc_reserve) / ltc_btc_sell1_price个LTC
     * 其中：btc_available表示该账户中可用的BTC数量，btc_reserve表示该账户中应该最少预留的BTC数量 （这个数值由用户根据自己的风险偏好来设置，越高代表用户风险偏好越低）。
     * 5.	LTC/CNY账户中可以用来卖的cny额度： cny_available – cny_reserve，相当于 (cny_available – cny_reserve) /
     * ltc_cny_sell1_price个LTC 其中，cny_available表示该账户中可用的人民币数量，cny_reserve表示该账户中应该最少预留的人民币数量
     * （这个数值由用户根据自己的风险偏好来设置，越高代表用户风险偏好越低）。
     *
     * '''
     */
    private float getMarketSellSize() {
        float size = 0;

        float
                marketSellSize =
                this.marketPrice.getBuyAmount()
                * order_ratio_base_quote;
        float
                baseMidBuySize =
                this.baseMidPrice.getSellAmount()
                * order_ratio_base_mid;

        //TODO size

        log.info("计算数量：{}，{}，{}，{}，{}", marketSellSize, baseMidBuySize);

        size =
                (float) (Math.floor(
                        Collections.min(ImmutableList.of(marketSellSize, baseMidBuySize)) * 10000)
                         / 10000);

        return size;
    }

    /**
     * ''' 正循环套利 正循环套利的顺序如下： 先去LTC/BTC吃单买入LTC，卖出BTC，然后根据LTC/BTC的成交量，使用多线程，
     * 同时在LTC/CNY和BTC/CNY市场进行对冲。LTC/CNY市场吃单卖出LTC，BTC/CNY市场吃单买入BTC。
     * <p>
     * '''
     */
    private Arbitrage posCycle(float marketBuySize) {
        log.info("开始正循环套利 size:{}", marketBuySize);

        //TODO buy
        float baseMidPriceBuy1 = this.baseMidPrice.getBuy();
        float quoteMidPriceSell1 = this.quoteMidPrice.getSell();
        float marketPriceSell1 = this.marketPrice.getSell();

        // base/quote 买入 base 需花费的 quote 数量
        float p3 = marketPriceSell1 * (1 + base_quote_slippage);

        // quote/mid 买入 quote 需花费的 mid 数量
        float p1 = quoteMidPriceSell1 * (1 + quote_mid_slippage);

        // base/mid 卖出 base 获得的 mid 数量
        float p2 = baseMidPriceBuy1 * (1 + base_mid_slippage);

        // 买入 marketBuySize 个 base 需要 卖出 quote 的数量
        float quoteSellSize = marketBuySize * p3 / (1 - base_quote_fee);

        // 买入 quoteSellSize 个 quote 需要 卖出 mid 的数量
        float midSizeForBuyQuote = quoteSellSize * p1 / (1 - quote_mid_fee);

        // 卖出 marketBuySize 个 base 获得 mid 的数量
        float midSizeFromSellBase = marketBuySize * p2 * (1 - base_mid_fee);

        float arbitrageValue = midSizeFromSellBase - midSizeForBuyQuote;

        float arbitragePecentage = arbitrageValue / midSizeForBuyQuote;

        Arbitrage arbitrage = new Arbitrage();
        arbitrage.setArbitrage(arbitrageValue);
        arbitrage.setPecentage(arbitragePecentage);
        arbitrage.setType(Arbitrage.TYPE_POS);

        return arbitrage;
    }

    /**
     * # 计算最保险的下单数量 ''' 1.	LTC/BTC卖方盘口吃单数量：ltc_btc_sell1_quantity*order_ratio_ltc_btc，其中ltc_btc_sell1_quantity
     * 代表LTC/BTC卖一档的数量， order_ratio_ltc_btc代表本策略在LTC/BTC盘口的吃单比例 2.	LTC/CNY买方盘口吃单数量：ltc_cny_buy1_quantity*order_ratio_ltc_cny，其中order_ratio_ltc_cny代表本策略在LTC/CNY盘口的吃单比例
     * 3.	LTC/BTC账户中可以用来买LTC的BTC额度及可以置换的LTC个数： btc_available - btc_reserve，可以置换成 (btc_available –
     * btc_reserve)/ltc_btc_sell1_price个LTC 其中，btc_available表示该账户中可用的BTC数量，btc_reserve表示该账户中应该最少预留的BTC数量
     * （这个数值由用户根据自己的风险偏好来设置，越高代表用户风险偏好越低）。 4.	BTC/CNY账户中可以用来买BTC的CNY额度及可以置换的BTC个数和对应的LTC个数：
     * cny_available - cny_reserve, 可以置换成 (cny_available-cny_reserve)/btc_cny_sell1_price个BTC， 相当于
     * (cny_available-cny_reserve)/btc_cny_sell1_price/ltc_btc_sell1_price 个LTC
     * 其中：cny_available表示该账户中可用的人民币数量，cny_reserve表示该账户中应该最少预留的人民币数量 （这个数值由用户根据自己的风险偏好来设置，越高代表用户风险偏好越低）。
     * 5.	LTC/CNY账户中可以用来卖的LTC额度： ltc_available – ltc_reserve 其中，ltc_available表示该账户中可用的LTC数量，ltc_reserve表示该账户中应该最少预留的LTC数量
     * （这个数值由用户根据自己的风险偏好来设置，越高代表用户风险偏好越低）。 '''
     */
    private float getMarketBuySize() {
        float size = 0;

        float
                marketBuySize =
                this.marketPrice.getSellAmount()
                * this.order_ratio_base_quote;

        float
                baseMidSellSize =
                this.baseMidPrice.getBuyAmount()
                * this.order_ratio_base_mid;

        float
                baseQuoteOffReserveBuySize =
                this.accountAvailable(quoteCur, new CurrencyPair(baseCur, quoteCur))
                - base_quote_quote_reserve;
        float
                quoteMidOffReserveBuySize =
                this.accountAvailable(midCur, new CurrencyPair(quoteCur, midCur))
                - quote_mid_mid_reserve;
        float
                baseMidOffReserveSellSize =
                this.accountAvailable(baseCur, new CurrencyPair(baseCur, midCur))
                - base_mid_base_reserve;

        log.info("计算数量：{}，{}，{}，{}，{}", marketBuySize, baseMidSellSize,
                 baseQuoteOffReserveBuySize, quoteMidOffReserveBuySize,
                 baseMidOffReserveSellSize);

        //TODO account size

        size =
                (float) (Math.floor(
                        Collections.min(ImmutableList.of(marketBuySize, baseMidSellSize)) * 10000)
                         / 10000);

        return size;
    }

    private float accountAvailable(Currency quoteCur, CurrencyPair currencyPair) {
        //TODO account available
        return 0;
    }

    /**
     * 正循环公允价和市场价的价差比例 (base_mid_price_buy_1 / quote_mid_price_sell_1 -
     * market_price_sell_1)/market_price_sell_1
     */
    private float posCycleFairPrice() {

        float baseMidPriceBuy1 = this.baseMidPrice.getBuy();
        float quoteMidPriceSell1 = this.quoteMidPrice.getSell();
        float marketPriceSell1 = this.marketPrice.getSell();

        return (baseMidPriceBuy1 / quoteMidPriceSell1 - marketPriceSell1) / marketPriceSell1;
    }

    /**
     * 逆循环公允价和市场价的价差比例 (market_price_buy_1 - base_mid_price_sell_1 / quote_mid_price_buy_1)/market_price_buy_1
     */
    private float negCycleFairPrice() {

        float marketPriceBuy1 = this.marketPrice.getBuy();
        float baseMidPriceSell1 = this.baseMidPrice.getSell();
        float quoteMidPriceBuy1 = this.quoteMidPrice.getBuy();

        return (marketPriceBuy1 - baseMidPriceSell1 / quoteMidPriceBuy1) / marketPriceBuy1;
    }

    /**
     * 所有市场的费率总和再加上滑点总和
     */
    private float sumSlippageFee() {
        return base_quote_slippage + base_mid_slippage + quote_mid_slippage +
               base_quote_fee + base_mid_fee + quote_mid_fee;
    }

    @Override
    public String toString() {
        return "TrianglePair{" +
               "quoteCur=" + quoteCur +
               ", midCur=" + midCur +
               ", baseCur=" + baseCur +
               ", negCycleFairPrice=" + negCycleFairPrice() +
               ", posCycleFairPrice=" + negCycleFairPrice() +
               '}';
    }
}
