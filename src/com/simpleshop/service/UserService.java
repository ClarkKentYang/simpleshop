package com.simpleshop.service;

import java.sql.SQLException;

import com.simpleshop.dao.UserDao;
import com.simpleshop.domain.User;

public class UserService {

	//注册
	public boolean regist(User user) {
		
		UserDao dao = new UserDao();
		int row = 0;
		try {
			row = dao.regist(user);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return row>0?true:false;
	}

	//激活
	public void active(String activeCode) {
		UserDao userDao = new UserDao();
		try {
			userDao.active(activeCode);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean checkUsername(String username) {
		UserDao dao = new UserDao();
		Long isExist = 0l;
		try {
			isExist = dao.checkUsername(username);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isExist>0?true:false;
	}

	public User login(String username, String password) throws SQLException {
		UserDao dao = new UserDao();
		return dao.login(username,password);
	}
	
}
