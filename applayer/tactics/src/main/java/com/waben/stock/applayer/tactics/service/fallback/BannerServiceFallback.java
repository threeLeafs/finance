package com.waben.stock.applayer.tactics.service.fallback;

import java.util.List;

import org.springframework.stereotype.Component;

import com.waben.stock.applayer.tactics.service.BannerService;
import com.waben.stock.interfaces.constants.ExceptionConstant;
import com.waben.stock.interfaces.dto.manage.BannerDto;
import com.waben.stock.interfaces.exception.ServiceException;

/**
 * 轮播 断路器回调
 * 
 * @author luomengan
 *
 */
@Component
public class BannerServiceFallback implements BannerService {

	@Override
	public List<BannerDto> getEnabledBannerList() {
		throw new ServiceException(ExceptionConstant.UNKNOW_EXCEPTION);
	}

}
