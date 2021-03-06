package com.waben.stock.interfaces.commonapi.juhe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import com.waben.stock.interfaces.commonapi.juhe.bean.BankVerifyBean;
import com.waben.stock.interfaces.commonapi.juhe.bean.JuheResponse;
import com.waben.stock.interfaces.constants.ExceptionConstant;
import com.waben.stock.interfaces.exception.ServiceException;
import com.waben.stock.interfaces.util.JacksonUtil;

/**
 * 银行卡四要素校验器
 * 
 * @author luomengan
 *
 */
public class BankCardInfoVerifier {

	public static final String key = "924c8c28247d09a2744aa7138a52192d";

	public static RestTemplate restTemplate = new RestTemplate();

	private static Logger logger = LoggerFactory.getLogger(BankCardInfoVerifier.class);

	public static boolean verify(String name, String idCard, String phone, String bankCard) {
		String requestUrl = String.format(
				"http://v.juhe.cn/verifybankcard4/query?key=%s&bankcard=%s&realname=%s&idcard=%s&mobile=%s", key,
				bankCard, name, idCard, phone);
		// 请求验证四要素
		logger.info("请求聚合四要素验证: {}", requestUrl);
		String response = restTemplate.getForObject(requestUrl, String.class);
		logger.info("聚合四要素验证响应: {}", response);
		JuheResponse<BankVerifyBean> responseObj = JacksonUtil.decode(response,
				JacksonUtil.getGenericType(JuheResponse.class, BankVerifyBean.class));
		if (responseObj.getError_code() == 0) {
			if (responseObj.getResult() != null && "1".equals(responseObj.getResult().getRes())) {
				return true;
			} else {
				return false;
			}
		} else if (responseObj.getError_code() == 10012) {
			throw new ServiceException(ExceptionConstant.UNKNOW_EXCEPTION, "验证接口可用次数不足!");
		} else {
			throw new ServiceException(ExceptionConstant.BANKCARDINFO_NOTMATCH_EXCEPTION, responseObj.getReason());
		}
	}

	public static void test(String[] args) {
		System.out.println(verify("李四", "410323199309021511", "13949111791", "6227002432210669566"));
	}

}
