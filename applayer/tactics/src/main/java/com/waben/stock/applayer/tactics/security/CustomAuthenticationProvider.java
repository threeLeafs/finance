package com.waben.stock.applayer.tactics.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.waben.stock.applayer.tactics.reference.PublisherReference;
import com.waben.stock.applayer.tactics.security.jwt.JWTTokenUtil;
import com.waben.stock.interfaces.dto.publisher.PublisherDto;
import com.waben.stock.interfaces.pojo.Response;
import com.waben.stock.interfaces.util.PasswordCrypt;

public class CustomAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	@Autowired
	private PublisherReference publisherReference;

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
		String password = null;
		if(authentication != null && authentication.getCredentials() != null) {
			password = authentication.getCredentials().toString();
		}
		if (!PasswordCrypt.match(password, customUserDetails.getPassword())) {
			throw new BadCredentialsException("密码错误");
		}
	}

	@Override
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		// APP用户
		Response<PublisherDto> publisherResp = publisherReference.fetchByPhone(username);
		if (!"200".equals(publisherResp.getCode())) {
			throw new UsernameNotFoundException("用户名不存在");
		}
		if (publisherResp.getResult() == null) {
			throw new UsernameNotFoundException("用户名不存在");
		}
		Long userId = publisherResp.getResult().getId();
		String password = publisherResp.getResult().getPassword();
		String serialCode = publisherResp.getResult().getSerialCode();
		return new CustomUserDetails(userId, serialCode, username, password, JWTTokenUtil.getAppGrantedAuthList());
	}

}
