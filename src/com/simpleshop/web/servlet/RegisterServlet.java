package com.simpleshop.web.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import com.simpleshop.domain.User;
import com.simpleshop.service.UserService;
import com.simpleshop.utils.CommonsUtils;

public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       

    public RegisterServlet() {
        super();
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		//获得表单数据
		Map<String,String[]> properties = request.getParameterMap();
		User user= new User();
		try {
			//指定一个类型转换器
			ConvertUtils.register(new Converter() {		
				@Override
				public Object convert(Class arg0, Object arg1) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date parse = null;
					try {
						parse = format.parse(arg1.toString());
					} catch (ParseException e) {
						e.printStackTrace();
					}
					return parse;
				}
			}, Date.class);
			//映射封装
			BeanUtils.populate(user, properties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		user.setUid(CommonsUtils.getUUID());
		user.setTelephone(null);
		user.setState(0);
		user.setCode(CommonsUtils.getUUID());
		
		UserService service = new UserService();
		
		boolean isRegisterSuccess = service.regist(user);
		
		if(isRegisterSuccess){
			response.sendRedirect(request.getContextPath()+"/registerSuccess.jsp");
		}else{
			response.sendRedirect(request.getContextPath()+"/registerFail.jsp");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
