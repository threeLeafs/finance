package com.waben.stock.applayer.promotion.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.waben.stock.applayer.promotion.reference.organization.OrganizationAccountReference;
import com.waben.stock.interfaces.dto.organization.OrganizationAccountDto;
import com.waben.stock.interfaces.exception.ServiceException;
import com.waben.stock.interfaces.pojo.Response;

/**
 * 机构账户 Business
 * 
 * @author luomengan
 *
 */
@Service
public class OrganizationAccountBusiness {

	@Autowired
	@Qualifier("organizationAccountReference")
	private OrganizationAccountReference reference;

	public OrganizationAccountDto fetchByOrgId(Long orgId) {
		Response<OrganizationAccountDto> response = reference.fetchByOrgId(orgId);
		if ("200".equals(response.getCode())) {
			return response.getResult();
		}
		throw new ServiceException(response.getCode());
	}

	public void modifyPaymentPassword(Long orgId, String oldPaymentPassword, String paymentPassword) {
		Response<Void> response = reference.modifyPaymentPassword(orgId, oldPaymentPassword, paymentPassword);
		if ("200".equals(response.getCode())) {
			return;
		}
		throw new ServiceException(response.getCode());
	}

}