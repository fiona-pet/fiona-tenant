package com.fionapet.tenant.tc.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;

import java.io.IOException;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Getter
@Setter
@ToString
@Slf4j
public class TrianglePair {
	public static final float ltc_btc_slippage = 0;
	public static final float btc_usd_slippage = 0;
	public static final float ltc_usd_slippage = 0;

	public static final float ltc_btc_fee = 0.001f;
	public static final float btc_usd_fee = 0.001f;
	public static final float ltc_usd_fee = 0.001f;

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

	private float convertPairSellPrice;
	private float fromBasePairSellPrice;
	private float toBasePairBuyPrice;

	private float convertPairRemainingAmount;
	private float fromBasePairRemainingAmount;
	private float toBasePairRemainingAmount;

	public Arbitrage arbitrage(){
		float arbitrageValue = 0;

        float ltc_btc_sell1_price = convertPairSellPrice;
        float btc_usd_sell1_price = fromBasePairSellPrice;
        float ltc_usd_buy1_price = toBasePairBuyPrice;

        // LTC/BTC 买入 LTC 需花费的 BTC数量
		float p3 = ltc_btc_sell1_price * (1 + ltc_btc_slippage);

        // BTC/USD 买入 BTC 需花费的 USD数量
		float p1 = btc_usd_sell1_price * (1 + btc_usd_slippage);

        // LTC/USD 卖出 BTC 获得的 USD数量
		float p2 = ltc_usd_buy1_price * (1 + ltc_usd_slippage);

		// 在LTC/BTC市场净买入1个LTC，实际上需要买入1/(1-ltc_btc_fee)个LTC，其中的ltc_btc_fee比例部分，是被交易平台收走的手续费。买入1/(1-ltc_btc_fee)个LTC需要花费的BTC数量是
		float buy_ltc_out_btc_num = p3 / (1 - ltc_btc_fee);

		// 在LTC/CNY市场，卖出1个LTC，得到的usd是
		float sell_ltc_in_usd_num = p2 * (1 - ltc_usd_fee);

        // 在BTC/usd市场，净买入 buy_ltc_out_btc_num 个 btc ，实际需要买入
        // ltc_btc_sell_1_price*(1+ltc_btc_slippage)/[(1-ltc_btc_fee)*(1-btc_usd_fee)] 个btc
        float buy_btc_out_usd_num = buy_ltc_out_btc_num * p1/(1-btc_usd_fee);

		arbitrageValue = sell_ltc_in_usd_num - buy_btc_out_usd_num ;

		float arbitragePecentage = (sell_ltc_in_usd_num - buy_btc_out_usd_num)/buy_btc_out_usd_num;


		Arbitrage arbitrage = new Arbitrage();
		arbitrage.setArbitrage(arbitrageValue);
		arbitrage.setPecentage(arbitragePecentage);

		log.debug("p3:convertPair:{}(convertPairSellPrice:{})->p2:fromBasePair:{}(fromBasePairSellPrice:{})->p1:toBasePair:{}(toBasePairBuyPrice:{}), -- arbitrage:{}",convertPair,  convertPairSellPrice, fromBasePair, fromBasePairSellPrice, toBasePair, toBasePairBuyPrice, arbitrage);

		return arbitrage;
	}
}
