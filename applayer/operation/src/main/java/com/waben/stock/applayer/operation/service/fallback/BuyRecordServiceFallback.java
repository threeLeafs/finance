package com.waben.stock.applayer.operation.service.fallback;

import com.waben.stock.applayer.operation.service.buyrecord.BuyRecordService;
import com.waben.stock.interfaces.constants.ExceptionConstant;
import com.waben.stock.interfaces.dto.buyrecord.BuyRecordDto;
import com.waben.stock.interfaces.exception.NetflixCircuitException;
import com.waben.stock.interfaces.pojo.Response;
import com.waben.stock.interfaces.pojo.query.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author Created by yuyidi on 2017/12/2.
 * @desc
 */
@Component
public class BuyRecordServiceFallback implements BuyRecordService {

    @Override
    public Response<BuyRecordDto> addBuyRecord(BuyRecordDto buyRecordDto) {
        throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
    }

    @Override
    public Response<String> dropBuyRecord(Long id) {
        throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
    }


    @Override
    public Response<BuyRecordDto> sellLock(Long investorId, Long id, String delegateNumber,
                                           String windControlTypeIndex) {
        throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
    }


    @Override
    public Response<PageInfo<BuyRecordDto>> pagesByQuery(BuyRecordQuery buyRecordQuery) {
        throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
    }

    @Override
    public Response<PageInfo<BuyRecordDto>> pagesByPostedQuery(StrategyPostedQuery strategyPostedQuery) {
        throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
    }

    @Override
    public Response<PageInfo<BuyRecordDto>> pagesByHoldingQuery(StrategyHoldingQuery strategyHoldingQuery) {
        throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
    }

    @Override
    public Response<PageInfo<BuyRecordDto>> pagesByUnwindQuery(StrategyUnwindQuery strategyUnwindQuery) {
        throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
    }

    @Override
    public Response<BuyRecordDto> fetchBuyRecord(Long buyrecord) {
        throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
    }

    @Override
    public Response<BuyRecordDto> buyLock(Long investorId, Long id, String delegateNumber) {
        throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
    }

    @Override
    public Response<BuyRecordDto> buyInto(Long investorId, Long id, BigDecimal buyingPrice) {
        throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
    }

    @Override
    public Response<BuyRecordDto> sellOut(Long investorId, Long id, BigDecimal sellingPrice) {
        throw new NetflixCircuitException(ExceptionConstant.NETFLIX_CIRCUIT_EXCEPTION);
    }

    @Override
    public Response<BuyRecordDto> sellApply(Long publisherId, Long id) {
        return null;
    }
}
