package com.waben.stock.interfaces.dto.stockcontent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.waben.stock.interfaces.vo.stockcontent.StockExponentVo;

/**
 * 股票
 *
 * @author luomengan
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockDto {

	private Long id;
	/**
	 * 股票名称
	 */
	private String name;
	/**
	 * 股票代码
	 */
	private String code;
	/**
	 * 缩写
	 */
	private String abbr;
	/**
	 * 股票状态(可买可卖状态 非开始闭市状态)
	 */
	private Boolean status;

	private StockExponentDto exponent;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public StockExponentDto getExponent() {
		return exponent;
	}

	public void setExponent(StockExponentDto exponent) {
		this.exponent = exponent;
	}

	public void setStockExponent(StockExponentDto stockExponent) {
		this.exponent = stockExponent;
	}

	public String getAbbr() {
		return abbr;
	}

	public void setAbbr(String abbr) {
		this.abbr = abbr;
	}

}
