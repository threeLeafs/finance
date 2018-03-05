package com.waben.stock.applayer.strategist.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.waben.stock.applayer.strategist.business.CapitalAccountBusiness;
import com.waben.stock.applayer.strategist.business.StockBusiness;
import com.waben.stock.applayer.strategist.business.StockOptionCycleBusiness;
import com.waben.stock.applayer.strategist.business.StockOptionQuoteBusiness;
import com.waben.stock.applayer.strategist.business.StockOptionTradeBusiness;
import com.waben.stock.applayer.strategist.dto.stockoption.StockOptionQuoteWithBalanceDto;
import com.waben.stock.applayer.strategist.dto.stockoption.StockOptionTradeWithMarketDto;
import com.waben.stock.applayer.strategist.security.SecurityUtil;
import com.waben.stock.interfaces.constants.ExceptionConstant;
import com.waben.stock.interfaces.dto.publisher.CapitalAccountDto;
import com.waben.stock.interfaces.dto.stockcontent.StockDto;
import com.waben.stock.interfaces.dto.stockoption.StockOptionCycleDto;
import com.waben.stock.interfaces.dto.stockoption.StockOptionQuoteDto;
import com.waben.stock.interfaces.dto.stockoption.StockOptionTradeDto;
import com.waben.stock.interfaces.enums.StockOptionBuyingType;
import com.waben.stock.interfaces.enums.StockOptionTradeState;
import com.waben.stock.interfaces.exception.ServiceException;
import com.waben.stock.interfaces.pojo.Response;
import com.waben.stock.interfaces.pojo.query.PageInfo;
import com.waben.stock.interfaces.pojo.query.StockOptionTradeUserQuery;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 期权交易 Controller
 * 
 * @author luomengan
 *
 */
@RestController
@RequestMapping("/stockoptiontrade")
@Api(description = "期权交易")
public class StockOptionTradeController {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private StockOptionCycleBusiness cycleBusiness;

	@Autowired
	private StockOptionQuoteBusiness quoteBusiness;

	@Autowired
	private CapitalAccountBusiness accountBusiness;

	@Autowired
	private StockOptionTradeBusiness tradeBusiness;

	@Autowired
	private CapitalAccountBusiness capitalAccountBusiness;

	@Autowired
	private StockBusiness stockBusiness;

	@GetMapping("/cyclelists")
	@ApiOperation(value = "期权周期列表")
	public Response<List<StockOptionCycleDto>> lists() {
		return new Response<>(cycleBusiness.lists());
	}

	@GetMapping("/{stockCode}/{cycle}/quote")
	@ApiOperation(value = "询价")
	public Response<StockOptionQuoteWithBalanceDto> quote(@PathVariable("stockCode") String stockCode,
			@PathVariable("cycle") Integer cycle) {
		StockOptionQuoteWithBalanceDto quote = new StockOptionQuoteWithBalanceDto(
				quoteBusiness.quote(stockCode, cycle));
		quote.setAvailableBalance(accountBusiness.findByPublisherId(SecurityUtil.getUserId()).getAvailableBalance());
		return new Response<>(quote);
	}

	@PostMapping("/buy")
	@ApiOperation(value = "申购", notes = "buyingType买入方式:1市价买入")
	public Response<StockOptionTradeWithMarketDto> buy(@RequestParam(required = true) Integer buyingType,
			@RequestParam(required = true) Long cycleId, @RequestParam(required = true) BigDecimal nominalAmount,
			@RequestParam(required = true) String stockCode, @RequestParam(required = true) String paymentPassword) {
		logger.info("APP调用接口发布人{}申购期权{}，名义本金{}!", SecurityUtil.getUserId(), stockCode, nominalAmount);
		// 检查股票是否可以购买，停牌、涨停、跌停不能购买
		stockBusiness.checkStock(stockCode);
		// 判断名义本金是否大于20万，且是否是10万的整数倍
		if (nominalAmount.compareTo(new BigDecimal("200000")) < 0) {
			throw new ServiceException(ExceptionConstant.STOCKOPTION_AMOUNTMUSTGT20WAN_EXCEPTION);
		}
		BigDecimal[] checkNominal = nominalAmount.divideAndRemainder(new BigDecimal("100000"));
		if (checkNominal[1].compareTo(new BigDecimal("0")) != 0) {
			throw new ServiceException(ExceptionConstant.STOCKOPTION_AMOUNTMUSTGT20WAN_EXCEPTION);
		}
		// 获取股票、期权周期、报价
		StockDto stock = stockBusiness.findByCode(stockCode);
		StockOptionCycleDto cycle = cycleBusiness.fetchById(cycleId);
		StockOptionQuoteDto quote = quoteBusiness.quote(stockCode, cycle.getCycle());
		// 验证支付密码
		CapitalAccountDto capitalAccount = capitalAccountBusiness.findByPublisherId(SecurityUtil.getUserId());
		String storePaymentPassword = capitalAccount.getPaymentPassword();
		if (storePaymentPassword == null || "".equals(storePaymentPassword)) {
			throw new ServiceException(ExceptionConstant.PAYMENTPASSWORD_NOTSET_EXCEPTION);
		}
		if (!storePaymentPassword.equals(paymentPassword)) {
			throw new ServiceException(ExceptionConstant.PAYMENTPASSWORD_WRONG_EXCEPTION);
		}
		// 检查余额
		BigDecimal rightMoney = nominalAmount.multiply(quote.getRightMoneyRatio()).setScale(2, RoundingMode.DOWN);
		if (rightMoney.compareTo(capitalAccount.getAvailableBalance()) > 0) {
			throw new ServiceException(ExceptionConstant.AVAILABLE_BALANCE_NOTENOUGH_EXCEPTION);
		}
		// 初始化点买数据
		StockOptionTradeDto dto = new StockOptionTradeDto();
		dto.setBuyingType(StockOptionBuyingType.getByIndex(String.valueOf(buyingType)));
		dto.setNominalAmount(nominalAmount);
		dto.setPublisherId(SecurityUtil.getUserId());
		dto.setPublisherPhone(SecurityUtil.getUsername());
		dto.setRightMoney(rightMoney);
		dto.setCycle(cycle.getCycle());
		dto.setCycleName(cycle.getName());
		dto.setRightMoneyRatio(quote.getRightMoneyRatio());
		dto.setStockCode(stockCode);
		dto.setStockName(stock.getName());

		StockOptionTradeDto tradeDto = tradeBusiness.add(dto);
		return new Response<>(tradeBusiness.wrapMarketInfo(tradeDto));
	}

	@GetMapping("/pagesHoldPosition")
	@ApiOperation(value = "持仓中的期权记录列表")
	public Response<PageInfo<StockOptionTradeWithMarketDto>> pagesHoldPosition(int page, int size) {
		StockOptionTradeUserQuery query = new StockOptionTradeUserQuery(page, size, SecurityUtil.getUserId(),
				new StockOptionTradeState[] { StockOptionTradeState.WAITCONFIRMED, StockOptionTradeState.TURNOVER,
						StockOptionTradeState.APPLYRIGHT, StockOptionTradeState.INSETTLEMENT });
		PageInfo<StockOptionTradeDto> pageInfo = tradeBusiness.pagesByUserQuery(query);
		List<StockOptionTradeWithMarketDto> content = tradeBusiness.wrapMarketInfo(pageInfo.getContent());
		return new Response<>(new PageInfo<>(content, pageInfo.getTotalPages(), pageInfo.getLast(),
				pageInfo.getTotalElements(), pageInfo.getSize(), pageInfo.getNumber(), pageInfo.getFrist()));
	}

	@GetMapping("/pagesUnwind")
	@ApiOperation(value = "结算的期权记录列表")
	public Response<PageInfo<StockOptionTradeWithMarketDto>> pagesUnwind(int page, int size) {
		StockOptionTradeUserQuery query = new StockOptionTradeUserQuery(page, size, SecurityUtil.getUserId(),
				new StockOptionTradeState[] { StockOptionTradeState.FAILURE, StockOptionTradeState.SETTLEMENTED });
		PageInfo<StockOptionTradeDto> pageInfo = tradeBusiness.pagesByUserQuery(query);
		List<StockOptionTradeWithMarketDto> content = tradeBusiness.wrapMarketInfo(pageInfo.getContent());
		return new Response<>(new PageInfo<>(content, pageInfo.getTotalPages(), pageInfo.getLast(),
				pageInfo.getTotalElements(), pageInfo.getSize(), pageInfo.getNumber(), pageInfo.getFrist()));
	}

}