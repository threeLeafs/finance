package com.waben.stock.applayer.tactics.dto.buyrecord;

import java.math.BigDecimal;

import com.waben.stock.interfaces.dto.buyrecord.BuyRecordDto;

/**
 * 点买记录+股票行情信息
 * 
 * @author luomengan
 *
 */
public class BuyRecordWithMarketDto extends BuyRecordDto {

	/**
	 * 股票名称
	 */
	private String stockName;
	/**
	 * 最新价
	 */
	private BigDecimal lastPrice;
	/**
	 * 涨跌价格
	 */
	private BigDecimal upDropPrice;
	/**
	 * 涨跌幅度
	 */
	private BigDecimal upDropSpeed;

	public String getStockName() {
		return stockName;
	}

	public void setStockName(String stockName) {
		this.stockName = stockName;
	}

	public BigDecimal getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(BigDecimal lastPrice) {
		this.lastPrice = lastPrice;
	}

	public BigDecimal getUpDropPrice() {
		return upDropPrice;
	}

	public void setUpDropPrice(BigDecimal upDropPrice) {
		this.upDropPrice = upDropPrice;
	}

	public BigDecimal getUpDropSpeed() {
		return upDropSpeed;
	}

	public void setUpDropSpeed(BigDecimal upDropSpeed) {
		this.upDropSpeed = upDropSpeed;
	}

	@Override
	public BigDecimal getProfitOrLoss() {
		BigDecimal profitOrLoss = super.getProfitOrLoss();
		if (profitOrLoss == null && getBuyingPrice() != null && getLastPrice() != null) {
			return new BigDecimal(getNumberOfStrand()).multiply(getLastPrice().subtract(getBuyingPrice()));
		}
		return super.getProfitOrLoss();
	}

}