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

	//    LTC/BTC: ltc_btc_fee
	//    BTC/USD: btc_usd_fee
	//    LTC/USD: ltc_usd_fee
	public static final float ltc_btc_fee = 0.001f;
	public static final float btc_usd_fee = 0.001f;
	public static final float ltc_usd_fee = 0.001f;

	public static float arbitrage_fee = 0.0f;


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

	float convertPairSellPrice;
	float fromBasePairSellPrice;
	float toBasePairBuyPrice;

	public void arbitrage(){
		// P3 = ltc_btc_sell1_price*(1+ltc_btc_slippage)

		float ltc_btc_sell1_price = convertPairSellPrice;

		float p3 = ltc_btc_sell1_price * (1 + ltc_btc_slippage);


		// P1= btc_usd_sell1_price*(1+btc_usd_slippage)

		float btc_usd_sell1_price = fromBasePairSellPrice;
		float p1 = btc_usd_sell1_price * (1 + btc_usd_slippage);


		// P2 = ltc_usd_buy1_price*(1-ltc_usd_slippage)

		float ltc_usd_buy1_price = toBasePairBuyPrice;

		float p2 = ltc_usd_buy1_price * (1 + ltc_usd_slippage);

		// 在LTC/BTC市场净买入1个LTC，实际上需要买入1/(1-ltc_btc_fee)个LTC，其中的ltc_btc_fee比例部分，是被交易平台收走的手续费。买入1/(1-ltc_btc_fee)个LTC需要花费的BTC数量是
		float sell_ltc_btc_sub = ltc_btc_sell1_price * (1 + ltc_btc_slippage) / (1 - ltc_btc_fee);

		// 在LTC/CNY市场，卖出1个LTC，得到的usd是
		float sell_ltc_usd_add = ltc_usd_buy1_price * (1 - ltc_usd_slippage) * (1 - ltc_usd_fee);

		// 在BTC/usd市场，净买入sell_ltc_btc_sub 个 btc ，实际需要买入
		// ltc_btc_sell_1_price*(1+ltc_btc_slippage)/[(1-ltc_btc_fee)*(1-btc_usd_fee)] 个btc
		float sell_ltc_usd_sub =
				btc_usd_sell1_price
				* (1 + btc_usd_slippage)
				* ltc_btc_sell1_price
				* (1 + ltc_btc_slippage)
				/ ((1 - ltc_btc_fee) * (1 - btc_usd_fee));

		if (sell_ltc_usd_add > sell_ltc_usd_sub) {
			arbitrage_fee += (sell_ltc_usd_add - sell_ltc_usd_sub);
		} else {
			log.info("p3:convertPair:{}(convertPairSellPrice:{})->p2:fromBasePair:{}(fromBasePairSellPrice:{})->p2:toBasePair:{}(toBasePairBuyPrice:{}), -- not arbitrage:{}",convertPair,  convertPairSellPrice, fromBasePair, fromBasePairSellPrice, toBasePair, toBasePairBuyPrice, sell_ltc_usd_add - sell_ltc_usd_sub);
		}

		log.warn("convertPair:{}(convertPairSellPrice:{})->fromBasePair:{}(fromBasePairSellPrice:{})->toBasePair:{}(toBasePairBuyPrice:{}), -- arbitrage:{}",convertPair,  convertPairSellPrice, fromBasePair, fromBasePairSellPrice, toBasePair, toBasePairBuyPrice, arbitrage_fee);
	}
}
