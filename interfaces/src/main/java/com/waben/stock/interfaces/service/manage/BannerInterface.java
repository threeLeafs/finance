package com.waben.stock.interfaces.service.manage;

import java.util.List;

import com.waben.stock.interfaces.dto.manage.PermissionDto;
import com.waben.stock.interfaces.pojo.query.BannerQuery;
import com.waben.stock.interfaces.pojo.query.PageInfo;
import com.waben.stock.interfaces.pojo.query.PermissionQuery;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.waben.stock.interfaces.dto.manage.BannerDto;
import com.waben.stock.interfaces.pojo.Response;

/**
 * 轮播 公共接口
 * 
 * @author luomengan
 *
 */
public interface BannerInterface {

	@RequestMapping(value = "/", method = RequestMethod.GET)
	Response<List<BannerDto>> fetchBanners(@RequestParam(value = "enable",required = false) Boolean enable);

	@RequestMapping(value = "/pages", method = RequestMethod.GET,consumes = MediaType.APPLICATION_JSON_VALUE)
	Response<PageInfo<BannerDto>> pages(@RequestBody BannerQuery query);


}
