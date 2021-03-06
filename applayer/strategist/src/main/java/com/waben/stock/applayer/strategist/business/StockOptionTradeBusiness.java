package com.waben.stock.applayer.strategist.business;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.waben.stock.applayer.strategist.dto.stockoption.StockOptionTradeDynamicDto;
import com.waben.stock.applayer.strategist.dto.stockoption.StockOptionTradeWithMarketDto;
import com.waben.stock.applayer.strategist.reference.StockOptionTradeReference;
import com.waben.stock.interfaces.commonapi.retrivestock.RetriveStockOverHttp;
import com.waben.stock.interfaces.commonapi.retrivestock.bean.StockMarket;
import com.waben.stock.interfaces.constants.ExceptionConstant;
import com.waben.stock.interfaces.dto.manage.AnalogDataDto;
import com.waben.stock.interfaces.dto.stockoption.StockOptionTradeDto;
import com.waben.stock.interfaces.enums.AnalogDataType;
import com.waben.stock.interfaces.enums.StockOptionTradeState;
import com.waben.stock.interfaces.exception.ServiceException;
import com.waben.stock.interfaces.pojo.Response;
import com.waben.stock.interfaces.pojo.query.PageInfo;
import com.waben.stock.interfaces.pojo.query.StockOptionTradeUserQuery;
import com.waben.stock.interfaces.util.CopyBeanUtils;
import com.waben.stock.interfaces.util.JacksonUtil;

/**
 * 期权交易 Business
 * 
 * @author luomengan
 *
 */
@Service
public class StockOptionTradeBusiness {
	
	Logger logger = LoggerFactory.getLogger(getClass());

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	@Qualifier("stockOptionTradeReference")
	private StockOptionTradeReference tradeReference;
	
	@Autowired
	private AnalogDataBusiness analogDataBusiness;

	public StockOptionTradeDto add(StockOptionTradeDto stockOptionTradeDto) {
		Response<StockOptionTradeDto> response = tradeReference.add(stockOptionTradeDto);
		if ("200".equals(response.getCode())) {
			return response.getResult();
		}
		throw new ServiceException(response.getCode());
	}

	public StockOptionTradeDto findById(Long id) {
		Response<StockOptionTradeDto> response = tradeReference.fetchById(id);
		if ("200".equals(response.getCode())) {
			return response.getResult();
		}
		throw new ServiceException(response.getCode());
	}

	public PageInfo<StockOptionTradeDto> pagesByUserQuery(StockOptionTradeUserQuery query) {
		Response<PageInfo<StockOptionTradeDto>> response = tradeReference.pagesByUserQuery(query);
		if ("200".equals(response.getCode())) {
			return response.getResult();
		}
		throw new ServiceException(response.getCode());
	}

	public StockOptionTradeDto userRight(Long publisherId, Long id) {
		StockOptionTradeDto trade = this.findById(id);
		// 持仓中的才能申请行权
		if (trade.getState() == StockOptionTradeState.TURNOVER && trade.getBuyingTime() != null
				&& !sdf.format(new Date()).equals(sdf.format(trade.getBuyingTime()))) {
			Response<StockOptionTradeDto> response = tradeReference.userRight(publisherId, id);
			if ("200".equals(response.getCode())) {
				return response.getResult();
			}
			throw new ServiceException(response.getCode());
		} else {
			throw new ServiceException(ExceptionConstant.USERRIGHT_NOTMATCH_EXCEPTION);
		}
	}

	public StockOptionTradeWithMarketDto wrapMarketInfo(StockOptionTradeDto trade) {
		List<StockOptionTradeDto> list = new ArrayList<>();
		list.add(trade);
		List<StockOptionTradeWithMarketDto> marketList = wrapMarketInfo(list);
		return marketList.get(0);
	}

	public List<StockOptionTradeWithMarketDto> wrapMarketInfo(List<StockOptionTradeDto> list) {
		List<StockOptionTradeWithMarketDto> result = CopyBeanUtils.copyListBeanPropertiesToList(list,
				StockOptionTradeWithMarketDto.class);
		List<String> codes = new ArrayList<>();
		for (StockOptionTradeWithMarketDto trade : result) {
			codes.add(trade.getStockCode());
		}
		if (codes.size() > 0) {
			List<StockMarket> stockMarketList = RetriveStockOverHttp.listStockMarket(restTemplate, codes);
			if (stockMarketList != null && stockMarketList.size() > 0) {
				for (int i = 0; i < stockMarketList.size(); i++) {
					StockMarket market = stockMarketList.get(i);
					StockOptionTradeWithMarketDto record = result.get(i);
					record.setStockName(market.getName());
					record.setLastPrice(market.getLastPrice());
					record.setUpDropPrice(market.getUpDropPrice());
					record.setUpDropSpeed(market.getUpDropSpeed());
					record.setStockStatus(market.getStatus());
				}
			}
		}
		return result;
	}

	public PageInfo<StockOptionTradeDynamicDto> tradeDynamic(int page, int size) {
		StockOptionTradeUserQuery query = new StockOptionTradeUserQuery(page, size / 2, null,
				new StockOptionTradeState[] { StockOptionTradeState.SETTLEMENTED });
		query.setOnlyProfit(true);
		Response<PageInfo<StockOptionTradeDto>> sResponse = tradeReference.pagesByUserQuery(query);
		if ("200".equals(sResponse.getCode())) {
			StockOptionTradeUserQuery bQuery = new StockOptionTradeUserQuery(page,
					size - sResponse.getResult().getContent().size(), null,
					new StockOptionTradeState[] { StockOptionTradeState.TURNOVER, StockOptionTradeState.APPLYRIGHT,
							StockOptionTradeState.INSETTLEMENT });
			PageInfo<StockOptionTradeDto> pageInfo = pagesByUserQuery(bQuery);
			int total = sResponse.getResult().getContent().size() + pageInfo.getContent().size();
			List<StockOptionTradeDynamicDto> content = new ArrayList<>();
			boolean isSettlement = true;
			for (int n = 0; n < total; n++) {
				if (isSettlement && sResponse.getResult().getContent().size() > 0) {
					StockOptionTradeDto settlement = sResponse.getResult().getContent().remove(0);
					StockOptionTradeDynamicDto inner = new StockOptionTradeDynamicDto();
					inner.setTradeType(2);
					inner.setPublisherId(settlement.getPublisherId());
					inner.setNominalAmount(settlement.getNominalAmount());
					inner.setStockCode(settlement.getStockCode());
					inner.setStockName(settlement.getStockName());
					inner.setPhone(settlement.getPublisherPhone());
					inner.setProfit(settlement.getProfit());
					inner.setTradePrice(settlement.getSellingPrice());
					inner.setTradeTime(settlement.getSellingTime());
					content.add(inner);
					isSettlement = false;
				} else {
					if (pageInfo.getContent().size() > 0) {
						StockOptionTradeDto trade = pageInfo.getContent().remove(0);
						StockOptionTradeDynamicDto inner = new StockOptionTradeDynamicDto();
						inner.setTradeType(1);
						inner.setPublisherId(trade.getPublisherId());
						inner.setNominalAmount(trade.getNominalAmount());
						inner.setStockCode(trade.getStockCode());
						inner.setStockName(trade.getStockName());
						inner.setPhone(trade.getPublisherPhone());
						inner.setProfit(trade.getProfit());
						inner.setTradePrice(trade.getBuyingPrice());
						inner.setTradeTime(trade.getBuyingTime());
						content.add(inner);
						isSettlement = true;
					} else {
						isSettlement = true;
						total++;
					}
				}

			}
			if (content.size() < size) {
				PageInfo<AnalogDataDto> analogDataPage = analogDataBusiness
						.pagesByType(AnalogDataType.STOCKOPTIONTRADEDYNAMIC, 0, size - content.size());
				if (analogDataPage.getContent().size() > 0) {
					for (AnalogDataDto analogData : analogDataPage.getContent()) {
						try {
							StockOptionTradeDynamicDto dynamic = JacksonUtil.decode(analogData.getData(),
									StockOptionTradeDynamicDto.class);
							if (content.size() > 0 && content.get(content.size() - 1).getTradeType() == 2) {
								dynamic.setTradeType(1);
							} else {
								dynamic.setTradeType(2);
							}
							content.add(dynamic);
						} catch (Exception ex) {
							logger.error("期权交易动态模拟数据格式错误?" + analogData.getData());
						}
					}
				}
			}
			return new PageInfo<StockOptionTradeDynamicDto>(content, 0, false, 0L, size, page, false);
		}
		throw new ServiceException(sResponse.getCode());
	}

}
