package com.waben.stock.interfaces.service.organization;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.waben.stock.interfaces.dto.organization.WithdrawalsApplyDto;
import com.waben.stock.interfaces.pojo.Response;
import com.waben.stock.interfaces.pojo.query.PageInfo;
import com.waben.stock.interfaces.pojo.query.WithdrawalsApplyQuery;

public interface WithdrawalsApplyInterface {

	/**
	 * 添加提现申请
	 * 
	 * @param apply
	 *            提现申请
	 * @return 提现申请
	 */
	@RequestMapping(value = "/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	Response<WithdrawalsApplyDto> addition(@RequestBody WithdrawalsApplyDto apply);

	/**
	 * 分页查询提现申请
	 *
	 * @param applyQuery
	 *            查询条件
	 * @return 提现申请列表
	 */
	@RequestMapping(value = "/pages", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	Response<PageInfo<WithdrawalsApplyDto>> pagesByQuery(@RequestBody WithdrawalsApplyQuery applyQuery);

	/**
	 * 修改状态
	 * 
	 * @param applyId
	 *            提现申请ID
	 * @param stateIndex
	 *            状态
	 * @return 提现申请
	 */
	@RequestMapping(value = "/{applyId}", method = RequestMethod.PUT)
	public Response<WithdrawalsApplyDto> changeState(@PathVariable("applyId") Long applyId,
			@RequestParam("stateIndex") String stateIndex);

}