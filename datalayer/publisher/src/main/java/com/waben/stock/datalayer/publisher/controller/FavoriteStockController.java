package com.waben.stock.datalayer.publisher.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waben.stock.datalayer.publisher.service.FavoriteStockService;
import com.waben.stock.interfaces.dto.publisher.FavoriteStockDto;
import com.waben.stock.interfaces.pojo.Response;
import com.waben.stock.interfaces.service.publisher.FavoriteStockInterface;
import com.waben.stock.interfaces.util.CopyBeanUtils;

/**
 * 收藏股票 Controller
 * 
 * @author luomengan
 *
 */
@RestController
@RequestMapping("/favoriteStock")
public class FavoriteStockController implements FavoriteStockInterface {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private FavoriteStockService favoriteStockService;

	@Override
	public Response<FavoriteStockDto> add(@RequestBody FavoriteStockDto favoriteStockDto) {
		return new Response<>(
				CopyBeanUtils.copyBeanProperties(
						FavoriteStockDto.class, favoriteStockService.save(favoriteStockDto.getPublisherId(),
								favoriteStockDto.getStockId(), favoriteStockDto.getName(), favoriteStockDto.getCode()),
						false));
	}

	@Override
	public Response<String> drop(@PathVariable Long publisherId, @PathVariable String stockCodes) {
		String[] stockCodeArr = stockCodes.split("-");
		favoriteStockService.remove(publisherId, stockCodeArr);
		return new Response<>("successful");
	}

	@Override
	public Response<String> top(@PathVariable Long publisherId, @PathVariable String stockCodes) {
		String[] stockCodeArr = stockCodes.split("-");
		favoriteStockService.top(publisherId, stockCodeArr);
		return new Response<>("successful");
	}

	@Override
	public Response<List<FavoriteStockDto>> listsByPublisherId(@PathVariable Long publisherId) {
		return new Response<>(CopyBeanUtils.copyListBeanPropertiesToList(favoriteStockService.list(publisherId),
				FavoriteStockDto.class));
	}

	@Override
	public Response<List<String>> listsStockCode(@PathVariable Long publisherId) {
		return new Response<>(favoriteStockService.listStockCodeByPublisherId(publisherId));
	}

}
