package com.waben.stock.datalayer.investors.reference.fallback;

import com.waben.stock.datalayer.investors.reference.StockReference;
import com.waben.stock.interfaces.constants.ExceptionConstant;
import com.waben.stock.interfaces.dto.stockcontent.StockDto;
import com.waben.stock.interfaces.exception.NetflixCircuitException;
import com.waben.stock.interfaces.pojo.Response;
import com.waben.stock.interfaces.pojo.query.PageInfo;
import com.waben.stock.interfaces.pojo.query.StockQuery;
import org.springframework.stereotype.Component;

/**
 * @author Created by yuyidi on 2017/11/22.
 * @desc
 */
@Component
public class StockReferenceFallback implements StockReference {
	@Override
	public Response<PageInfo<StockDto>> pagesByQuery(StockQuery stockQuery) {
		throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
	}

	@Override
	public Response<StockDto> fetchById(Long id) {
		throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
	}

	@Override
	public Response<StockDto> fetchWithExponentByCode(String code) {
		throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
	}

	@Override
	public Response<Integer> modify(StockDto stockDto) {
		throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);	}

	@Override
	public void delete(Long id) {

	}

	@Override
	public Response<StockDto> add(StockDto requestDto) {
		return new Response<>(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
	}
}
