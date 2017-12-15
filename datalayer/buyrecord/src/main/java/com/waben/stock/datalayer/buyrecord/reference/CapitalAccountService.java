package com.waben.stock.datalayer.buyrecord.reference;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Primary;

import com.waben.stock.datalayer.buyrecord.reference.fallback.CapitalAccountReferenceFallback;
import com.waben.stock.datalayer.buyrecord.warpper.FeignConfiguration;
import com.waben.stock.interfaces.service.publisher.CapitalAccountInterface;

/**
 * 资金账号 reference服务接口
 * 
 * @author luomengan
 *
 */
@FeignClient(name = "publisher/publisher", path = "capitalAccount", fallback = CapitalAccountReferenceFallback.class, configuration = FeignConfiguration.class)
@Primary
public interface CapitalAccountService extends CapitalAccountInterface {

}