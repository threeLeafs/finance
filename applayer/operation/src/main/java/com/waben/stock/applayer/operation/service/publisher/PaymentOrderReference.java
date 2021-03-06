package com.waben.stock.applayer.operation.service.publisher;

import org.springframework.cloud.netflix.feign.FeignClient;

import com.waben.stock.applayer.operation.service.fallback.PaymentOrderReferenceFallback;
import com.waben.stock.interfaces.service.publisher.PaymentOrderInterface;

/**
 * 支付订单 reference服务接口
 *
 * @author luomengan
 */
@FeignClient(name = "publisher", path = "paymentorder", fallback = PaymentOrderReferenceFallback.class, qualifier = "paymentOrderReference")
public interface PaymentOrderReference extends PaymentOrderInterface {

}
