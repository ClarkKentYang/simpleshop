package com.simpleshop.service;

import java.sql.SQLException;

import com.simpleshop.dao.UserDao;
import com.simpleshop.domain.User;

public class UserService {

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
	
}
