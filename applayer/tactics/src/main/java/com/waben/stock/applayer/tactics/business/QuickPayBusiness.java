package com.waben.stock.applayer.tactics.business;

import com.alibaba.fastjson.JSONObject;
import com.netflix.client.http.HttpRequest;
import com.waben.stock.applayer.tactics.payapi.caituopay.config.PayConfig;
import com.waben.stock.applayer.tactics.payapi.paypal.config.PayPalConfig;
import com.waben.stock.applayer.tactics.payapi.paypal.config.RSAUtil;
import com.waben.stock.applayer.tactics.payapi.shande.bean.PayRequestBean;
import com.waben.stock.applayer.tactics.payapi.shande.config.SandPayConfig;
import com.waben.stock.applayer.tactics.payapi.shande.utils.FormRequest;
import com.waben.stock.applayer.tactics.reference.PaymentOrderReference;
import com.waben.stock.applayer.tactics.reference.PublisherReference;
import com.waben.stock.applayer.tactics.reference.WithdrawalsOrderReference;
import com.waben.stock.interfaces.constants.ExceptionConstant;
import com.waben.stock.interfaces.dto.publisher.BindCardDto;
import com.waben.stock.interfaces.dto.publisher.PaymentOrderDto;
import com.waben.stock.interfaces.dto.publisher.PublisherDto;
import com.waben.stock.interfaces.dto.publisher.WithdrawalsOrderDto;
import com.waben.stock.interfaces.enums.PaymentState;
import com.waben.stock.interfaces.enums.PaymentType;
import com.waben.stock.interfaces.enums.WithdrawalsState;
import com.waben.stock.interfaces.exception.ServiceException;
import com.waben.stock.interfaces.pojo.Response;
import com.waben.stock.interfaces.util.JacksonUtil;
import com.waben.stock.interfaces.util.UniqueCodeGenerator;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class QuickPayBusiness {

    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    @Qualifier("paymentOrderReference")
    private PaymentOrderReference paymentOrderReference;
    @Autowired
    @Qualifier("publisherReference")
    private PublisherReference publisherReference;
    @Autowired
    @Qualifier("withdrawalsOrderReference")
    private WithdrawalsOrderReference withdrawalsOrderReference;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    @Autowired
    private CapitalAccountBusiness accountBusiness;

    @Autowired
    private BindCardBusiness bindCardBusiness;

    public PaymentOrderDto savePaymentOrder(PaymentOrderDto paymentOrder) {
        Response<PaymentOrderDto> orderResp = paymentOrderReference.addPaymentOrder(paymentOrder);
        if ("200".equals(orderResp.getCode())) {
            return orderResp.getResult();
        }
        throw new ServiceException(orderResp.getCode());
    }

    public Map<String, String> quickpay(BigDecimal amount, String phone) {

        Response<PublisherDto> response = publisherReference.fetchByPhone(phone);
        if (!"200".equals(response.getCode())) {
            throw new ServiceException(response.getCode());
        }
        //创建订单
        PaymentOrderDto paymentOrder = new PaymentOrderDto();
        paymentOrder.setAmount(amount);
        String paymentNo = UniqueCodeGenerator.generatePaymentNo();
        paymentOrder.setPaymentNo(paymentNo);
        paymentOrder.setType(PaymentType.QuickPay);
        paymentOrder.setState(PaymentState.Unpaid);
        paymentOrder.setPublisherId(response.getResult().getId());
        paymentOrder.setCreateTime(new Date());
        paymentOrder.setUpdateTime(new Date());
        this.savePaymentOrder(paymentOrder);
        //封装请求参数
        PayRequestBean request = new PayRequestBean();
        request.setUserId(phone.substring(1));
        request.setMchNo(SandPayConfig.mchNo);
        request.setMchType(SandPayConfig.mchType);
        request.setPayChannel(SandPayConfig.payChannel);
        request.setPayChannelTypeNo(SandPayConfig.payChannelTypeNo);
        request.setNotifyUrl(SandPayConfig.notifyUrl);
        request.setFrontUrl(SandPayConfig.fontUrl);
        request.setOrderNo(paymentNo);
        request.setAmount(amount.toString());
        request.setGoodsName(SandPayConfig.goodsName);
        request.setGoodsDesc(SandPayConfig.goodsDesc);
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = format.format(date);
        request.setTimeStamp(timeStamp);
        Map<String, String> paramMap = (Map<String, String>) JacksonUtil.decode(JacksonUtil.encode(request),
                Map.class);
        TreeMap<String, String> sortParamMap = new TreeMap<>(paramMap);
        StringBuilder strForSign = new StringBuilder();
        String toSign = "";
        for (String key : sortParamMap.keySet()) {
            toSign += key + "=" + sortParamMap.get(key) + "&";
        }
        toSign += "key=" + SandPayConfig.key;
        System.out.println(toSign);
        String sign = DigestUtils.md5Hex(toSign);
        sortParamMap.put("sign", sign);
        return sortParamMap;
    }

    public String sdPayReturn() {
        StringBuilder result = new StringBuilder();
        result.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>回调页面</title></head><body>");
        String paymentNo = "";
        String stateStr = "已支付";
        String scriptContent = "<script>function call() {if(window.appInterface) {window.appInterface.rechargeCallback('%s', '%s');} else {window.webkit.messageHandlers.callback.postMessage({paymentNo:'%s',result:'%s'});}} call();</script></body></html>";
        result.append(String.format(scriptContent, paymentNo, stateStr, paymentNo, stateStr));
        return result.toString();
    }

    public String sdPaycallback(HttpServletRequest request) {
        logger.info("回调开始");
        Map<String, String> responseResult = paramter2Map(request);
        Map<String, String> resultMap = new TreeMap<>(responseResult);
        logger.info("通知的参数是:{}", resultMap.toString());
        String toSign = "";
        for (String key : resultMap.keySet()) {
            if (!"msg".equals(key) && !"result".equals(key) && !"sign".equals(key)) {
                if (!StringUtils.isBlank(resultMap.get(key))) {
                    toSign += key + "=" + resultMap.get(key) + "&";
                }
            }
        }
        toSign += "key=" + SandPayConfig.key;
        logger.info("回调签名验证原串:{}", toSign);
        String resultSign = DigestUtils.md5Hex(toSign);
        logger.info("回调签名验证加密串:{}", resultSign);
        String result = responseResult.get("result");
        String sign = responseResult.get("sign");
        logger.info("通知签名串:{}", sign);
        String paymentNo = responseResult.get("orderNo");
        String thirdPaymentNo = responseResult.get("gwTradeNo");
        try {
            if (paymentNo != null && !"".equals(paymentNo) && "SUCCESS".equals(result)) {
                //验证签名
                if (sign.equals(resultSign)) {
                    logger.info("快捷移动验证签名成功");
                    // 支付成功
                    payCallback(paymentNo, PaymentState.Paid);
                }
                return "success";
            }
            return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root><retcode>207267</retcode><retmsg></retmsg>支付失败</root>";
        } catch (Exception ex) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root><retcode>207267</retcode><retmsg></retmsg>校验签名失败</root>";
        }
    }


    public String qqPaycallback(HttpServletRequest request) {
        Map<String, String> responseResult = paramter2Map(request);
        logger.info("QQ支付回调的结果是{}:", responseResult.toString());
        String result = responseResult.get("result");
        String paymentNo = responseResult.get("Out_trade_no");
        String message = responseResult.get("message");
        String thirdPaymentNo = responseResult.get("order_no");
        String code = responseResult.get("code");
        String sign = responseResult.get("sign");
        String resultSign = DigestUtils.md5Hex(PayConfig.appId + thirdPaymentNo + PayConfig.appSecret).toUpperCase();
        if (paymentNo != null && !"".equals(paymentNo) && "1".equals(code)) {
            //验证签名
            logger.info("QQ验证签名通过");
            if (sign.equals(resultSign)) {
                payCallback(paymentNo, PaymentState.Paid);
                return "success";
            }
            return "false";
        }
        return "false";
    }


    public String jdPaycallback(HttpServletRequest request) {
        Map<String, String> responseResult = paramter2Map(request);
        logger.info("京东支付回调的结果是{}:", responseResult.toString());
        String result = responseResult.get("result");
        String paymentNo = responseResult.get("Out_trade_no");
        String message = responseResult.get("message");
        String thirdPaymentNo = responseResult.get("order_no");
        String code = responseResult.get("code");
        String sign = responseResult.get("sign");
        String resultSign = DigestUtils.md5Hex(PayConfig.appId + thirdPaymentNo + PayConfig.appSecret).toUpperCase();
        if (paymentNo != null && !"".equals(paymentNo) && "1".equals(code)) {
            // 支付成功,验证签名
            if (sign.equals(resultSign)) {
                logger.info("京东验证签名通过");
                payCallback(paymentNo, PaymentState.Paid);
                return "success";
            }
            return "false";
        }
        return "false";
    }


    public void withdrawals(Long publisherId, BigDecimal amount, String name, String phone, String idCard,
                            String bankCard, String bankCode, String branchName) {
        //生成提现订单
        logger.info("保存提现订单");
        WithdrawalsOrderDto order = new WithdrawalsOrderDto();
        String withdrawalsNo = UniqueCodeGenerator.generateWithdrawalsNo();
        order.setWithdrawalsNo(withdrawalsNo);
        order.setAmount(amount);
        order.setState(WithdrawalsState.PROCESSING);
        order.setName(name);
        order.setIdCard(idCard);
        order.setBankCard(bankCard);
        order.setPublisherId(publisherId);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        this.saveWithdrawalsOrder(order);
        // 请求提现
        SimpleDateFormat time = new SimpleDateFormat("yyyyMMddHHmmss");
        Map<String, String> map = new TreeMap();
        map.put("mchNo", SandPayConfig.mchNo);
        map.put("payChannel", SandPayConfig.payChannel);
        map.put("orderNo", withdrawalsNo);
        map.put("amount", amount.toString());
        map.put("bankType", SandPayConfig.bankType);
        map.put("accNo", bankCard);
        map.put("accName", name);
        map.put("bankName", "中国银行");
        map.put("timeStamp", time.format(new Date()));
        //签名
        String toSign = "";
        for (String key : map.keySet()) {
            toSign += key + "=" + map.get(key) + "&";
        }
        toSign += "key=" + SandPayConfig.key;
        logger.info("代付签名的参数是:{}", toSign);
        String sign = DigestUtils.md5Hex(toSign);
        map.put("sign", sign);
        logger.info("代付的参数是:{}", map.toString());
        logger.info("代付请求发起");
        String result = FormRequest.doPost(map, SandPayConfig.csaUrl);
        JSONObject jsStr = JSONObject.parseObject(result);
        System.out.println(jsStr.toString());
        logger.info("代付请求的结果是:{}", jsStr);
//        CzWithholdResponse resp = CzWithholdOverSocket.withhold(withdrawalsNo, name, bankCard, phone, bankCode, amount);
//        // 提现异常
        JSONObject jsonData = jsStr.getJSONObject("data");
        String resultFlag = jsonData.getString("resultFlag");
        if ("SUCCESS".equals(jsStr.getString("result")) || "0".equals(resultFlag) || "2".equals(resultFlag)) {
            WithdrawalsOrderDto origin = findWithdrawalsOrder(withdrawalsNo);
            accountBusiness.csa(origin.getPublisherId(), origin.getAmount());
            if (origin.getState() != WithdrawalsState.PROCESSED) {
                // 更新代付订单的状态
                withdrawalsOrderReference.changeState(withdrawalsNo, WithdrawalsState.PROCESSED.getIndex());
            }
        } else {
            throw new ServiceException(ExceptionConstant.WITHDRAWALS_EXCEPTION, jsStr.getString("msg"));
        }


    }


    public Response<Map> ctQQh5(BigDecimal amount, String phone) {
        Response<PublisherDto> response = publisherReference.fetchByPhone(phone);
        if (!"200".equals(response.getCode())) {
            throw new ServiceException(response.getCode());
        }
        //创建订单
        PaymentOrderDto paymentOrder = new PaymentOrderDto();
        paymentOrder.setAmount(amount);
        String paymentNo = UniqueCodeGenerator.generatePaymentNo();
        paymentOrder.setPaymentNo(paymentNo);
        paymentOrder.setType(PaymentType.QuickPay);
        paymentOrder.setState(PaymentState.Unpaid);
        paymentOrder.setPublisherId(response.getResult().getId());
        paymentOrder.setCreateTime(new Date());
        paymentOrder.setUpdateTime(new Date());
        this.savePaymentOrder(paymentOrder);
        //封装请求参数
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
        String timeStamp = format.format(date);
        Map<String, String> map = new TreeMap<>();
        String appId = PayConfig.appId;
        String appSecret = PayConfig.appSecret;
        String url = PayConfig.qqurl;
        map.put("appId", appId);
        map.put("version", PayConfig.version);
        map.put("return_url", PayConfig.qq_return_url);
        map.put("device_info", PayConfig.device_info);
        map.put("mch_app_name", PayConfig.mch_app_name);
        map.put("mch_app_id", PayConfig.mch_app_id);
        map.put("total_fee", amount.toString());
        map.put("front_skip_url", PayConfig.qq_front_skip_url);
        map.put("subject", PayConfig.subject);
        map.put("body", PayConfig.qqbody);
        map.put("out_order_no", paymentNo);
        map.put("timestamp", timeStamp);
        String sign = DigestUtils.md5Hex(appId + appSecret + timeStamp + paymentNo);
        map.put("sign", sign);
        logger.info("请求的参数是{}:", map.toString());
        String result = FormRequest.doPost(map, url);
        JSONObject jsStr = JSONObject.parseObject(result);
        String payUrl = jsStr.getString("payUrl");
        logger.info("请求的结果是{}:", jsStr.toString());
        Response<Map> resp = new Response<Map>();
        if ("1".equals(jsStr.getString("code"))) {
            resp.setCode("200");
            resp.setMessage(jsStr.getString("message"));
            Map<String, String> resultUrl = new HashMap<>();
            resultUrl.put("url", payUrl);
            resp.setResult(resultUrl);
        } else {
            resp.setCode(jsStr.getString("code"));
            resp.setMessage(jsStr.getString("message"));
        }
        if (StringUtils.isBlank(payUrl)) {
            throw new ServiceException(ExceptionConstant.RECHARGE_EXCEPTION, jsStr.getString("message"));
        }
        return resp;
    }

    public Response<Map> jdh5(BigDecimal amount, String phone) {
        Response<PublisherDto> response = publisherReference.fetchByPhone(phone);
        if (!"200".equals(response.getCode())) {
            throw new ServiceException(response.getCode());
        }
        //创建订单
        PaymentOrderDto paymentOrder = new PaymentOrderDto();
        paymentOrder.setAmount(amount);
        String paymentNo = UniqueCodeGenerator.generatePaymentNo();
        paymentOrder.setPaymentNo(paymentNo);
        paymentOrder.setType(PaymentType.QuickPay);
        paymentOrder.setState(PaymentState.Unpaid);
        paymentOrder.setPublisherId(response.getResult().getId());
        paymentOrder.setCreateTime(new Date());
        paymentOrder.setUpdateTime(new Date());
        this.savePaymentOrder(paymentOrder);
        //封装请求参数
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
        String timeStamp = format.format(date);
        Map<String, String> map = new TreeMap<>();
        String appId = PayConfig.appId;
        String appSecret = PayConfig.appSecret;
        String url = PayConfig.jdurl;
        map.put("appId", appId);
        map.put("version", PayConfig.version);
        map.put("return_url", PayConfig.jd_return_url);
        map.put("total_fee", amount.toString());
        map.put("front_skip_url", PayConfig.jd_front_skip_url);
        map.put("subject", PayConfig.subject);
        map.put("body", PayConfig.jdbody);
        map.put("out_order_no", paymentNo);
        map.put("timestamp", timeStamp);
        String sign = DigestUtils.md5Hex(appId + appSecret + timeStamp + paymentNo);
        map.put("sign", sign);
        logger.info("请求的参数是{}:", map.toString());
        String result = FormRequest.doPost(map, url);
        JSONObject jsStr = JSONObject.parseObject(result);
        String payUrl = jsStr.getString("payUrl");
        logger.info("请求的结果是{}:", jsStr.toString());
        Response<Map> resp = new Response();
        if ("1".equals(jsStr.getString("code"))) {
            resp.setCode("200");
            resp.setMessage(jsStr.getString("message"));
            Map<String, String> resultUrl = new HashMap<>();
            resultUrl.put("url", payUrl);
            resp.setResult(resultUrl);
        } else {
            resp.setCode(jsStr.getString("code"));
            resp.setMessage(jsStr.getString("message"));
        }
        if (StringUtils.isBlank(payUrl)) {
            throw new ServiceException(ExceptionConstant.RECHARGE_EXCEPTION, jsStr.getString("message"));
        }
        return resp;
    }

    public Response<Map> payPal(BigDecimal amount, Long bindCardId, Long publisherId) {
        BindCardDto bindCard = bindCardBusiness.findById(bindCardId);
        if (bindCard == null) {
            throw new ServiceException(ExceptionConstant.DATANOTFOUND_EXCEPTION);
        }
        if (publisherId.compareTo(bindCard.getResourceId()) != 0) {
            throw new ServiceException(ExceptionConstant.PUBLISHERID_NOTMATCH_EXCEPTION);
        }
        Response<PublisherDto> response = publisherReference.fetchById(publisherId);
        if (!"200".equals(response.getCode())) {
            throw new ServiceException(response.getCode());
        }
        //创建订单
        PaymentOrderDto paymentOrder = new PaymentOrderDto();
        paymentOrder.setAmount(amount);
        String paymentNo = UniqueCodeGenerator.generatePaymentNo();
        paymentOrder.setPaymentNo(paymentNo);
        paymentOrder.setType(PaymentType.QuickPay);
        paymentOrder.setState(PaymentState.Unpaid);
        paymentOrder.setPublisherId(publisherId);
        paymentOrder.setCreateTime(new Date());
        paymentOrder.setUpdateTime(new Date());
        this.savePaymentOrder(paymentOrder);
        logger.info("订单保存成功,开始封装请求参数");
        //封装请求参数
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
        String timeStamp = format.format(date);
        JSONObject risk = new JSONObject();
        Map<String, String> map = new TreeMap<>();
        map.put("version", PayPalConfig.version);
        map.put("oid_partner", PayPalConfig.oid_partner);
        map.put("user_id", bindCard.getPhone().substring(1));
        map.put("app_request", PayPalConfig.app_request);
        map.put("sign_type", PayPalConfig.sign_type);
        map.put("busi_partner", PayPalConfig.busi_partner);//虚拟交易 实物交易
        map.put("no_order", paymentNo);
        map.put("dt_order", timeStamp);
        map.put("name_goods", PayPalConfig.name_goods);
        map.put("money_order", amount.toString());
        map.put("notify_url", PayPalConfig.notify_url);
        map.put("url_return", PayPalConfig.url_return);
        map.put("id_type", PayPalConfig.id_type);
        map.put("id_no", bindCard.getIdCard());
        map.put("acct_name", bindCard.getName());
        risk.put("frms_ware_category", PayPalConfig.frms_ware_category);
        risk.put("user_info_mercht_userno", bindCard.getPhone().substring(1));
        risk.put("user_info_dt_registe", sdf.format(response.getResult().getCreateTime()));
        risk.put("user_info_bind_phone", bindCard.getPhone());
        risk.put("user_info_identify_state", PayPalConfig.user_info_identify_state);
        risk.put("user_info_identify_type", PayPalConfig.user_info_identify_type);
        risk.put("user_info_full_name", bindCard.getName());
        risk.put("user_info_id_no", bindCard.getIdCard());
        map.put("risk_item", risk.toString());
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(map);
        String tosign = genSignData(jsonObject);
        String sign = RSAUtil.sign(PayPalConfig.private_key, tosign);
        map.put("sign", sign);
        JSONObject jsonObject1 = (JSONObject) JSONObject.toJSON(map);
        Map<String, String> masp = new HashMap<>();
        masp.put("req_data", jsonObject1.toJSONString());
        String result = FormRequest.doPost(masp, PayPalConfig.url);
        if (StringUtils.isBlank(result)) {
            throw new ServiceException(ExceptionConstant.RECHARGE_EXCEPTION, "请求异常");
        }
        Response<Map> resp = new Response();
        resp.setCode("200");
        resp.setMessage("请求成功");
        Map<String, String> resultUrl = new HashMap<>();
        resultUrl.put("url", result);
        resp.setResult(resultUrl);
        return resp;
    }


    public void payCallback(String paymentNo, PaymentState state) {
        PaymentOrderDto origin = findByPaymentNo(paymentNo);
        if (origin.getState() != state) {
            // 更新支付订单的状态
            logger.info("更新支付订单的状态");
            changeState(paymentNo, state);
            // 给发布人账号中充值
            if (state == PaymentState.Paid) {
                logger.info("给发布人账号中充值");
                accountBusiness.recharge(origin.getPublisherId(), origin.getAmount());
            }
        }
    }

    public PaymentOrderDto findByPaymentNo(String paymentNo) {
        Response<PaymentOrderDto> orderResp = paymentOrderReference.fetchByPaymentNo(paymentNo);
        if ("200".equals(orderResp.getCode())) {
            return orderResp.getResult();
        }
        throw new ServiceException(orderResp.getCode());
    }

    public PaymentOrderDto changeState(String paymentNo, PaymentState state) {
        Response<PaymentOrderDto> orderResp = paymentOrderReference.changeState(paymentNo, state.getIndex());
        if ("200".equals(orderResp.getCode())) {
            return orderResp.getResult();
        }
        throw new ServiceException(orderResp.getCode());
    }

    public WithdrawalsOrderDto saveWithdrawalsOrder(WithdrawalsOrderDto withdrawalsOrderDto) {
        Response<WithdrawalsOrderDto> orderResp = withdrawalsOrderReference.saveWithdrawalsOrders(withdrawalsOrderDto, withdrawalsOrderDto.getWithdrawalsNo());
        if ("200".equals(orderResp.getCode())) {
            return orderResp.getResult();
        }
        throw new ServiceException(orderResp.getCode());
    }

    public WithdrawalsOrderDto findWithdrawalsOrder(String withdrawalsNo) {
        Response<WithdrawalsOrderDto> orderResp = withdrawalsOrderReference.fetchByWithdrawalsNo(withdrawalsNo);
        if ("200".equals(orderResp.getCode())) {
            return orderResp.getResult();
        }
        throw new ServiceException(orderResp.getCode());
    }

    private Map<String, String> paramter2Map(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用。
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"),
            // "utf-8");
            params.put(name, valueStr);
        }
        return params;
    }

    public static String genSignData(com.alibaba.fastjson.JSONObject jsonObject) {
        StringBuffer content = new StringBuffer();

        // 按照key做首字母升序排列
        List<String> keys = new ArrayList<String>(jsonObject.keySet());
        Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < keys.size(); i++) {
            String key = (String) keys.get(i);
            if ("sign".equals(key)) {
                continue;
            }
            String value = jsonObject.getString(key);
            // 空串不参与签名
            if (isnull(value)) {
                continue;
            }
            content.append((i == 0 ? "" : "&") + key + "=" + value);

        }
        String signSrc = content.toString();
        if (signSrc.startsWith("&")) {
            signSrc = signSrc.replaceFirst("&", "");
        }
        return signSrc;
    }

    public static boolean isnull(String str) {
        if (null == str || str.equalsIgnoreCase("null") || str.equals("")) {
            return true;
        } else
            return false;
    }
}
