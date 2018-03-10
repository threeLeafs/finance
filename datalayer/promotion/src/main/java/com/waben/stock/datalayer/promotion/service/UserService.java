package com.waben.stock.datalayer.promotion.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waben.stock.datalayer.promotion.entity.User;
import com.waben.stock.datalayer.promotion.repository.UserDao;

/**
 * 机构管理用户 Service
 * 
 * @author luomengan
 *
 */
@Service
public class UserService {

	@Autowired
	private UserDao userDao;

	public User getUserInfo(Long id) {
		return userDao.retrieve(id);
	}

	@Transactional
	public User addUser(User user) {
		return userDao.create(user);
	}

	@Transactional
	public User modifyUser(User user) {
		return userDao.update(user);
	}

	@Transactional
	public void deleteUser(Long id) {
		userDao.delete(id);
	}
	
	@Transactional
	public void deleteUsers(String ids) {
		if(ids != null) {
			String[] idArr= ids.split(",");
			for(String id : idArr) {
				if(!"".equals(id.trim())) {
					userDao.delete(Long.parseLong(id.trim()));
				}
			}
		}
	}

	public Page<User> users(int page, int limit) {
		return userDao.page(page, limit);
	}
	
	public List<User> list() {
		return userDao.list();
	}

}