package com.simpleshop.web.servlet;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
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
import com.simpleshop.utils.MailUtils;

/**
 * Servlet implementation class UserServlet
 */
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public UserServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
		
		String method = request.getParameter("method");
		switch (method) {
		case "active":
			active(request,response);
			break;
		case "checkUsername":
			checkUsername(request,response);
			break;
		case "register":
			register(request,response);
			break;

		default:
			break;
		}
	
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	
	protected void active(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//获取激活码
		String activeCode = request.getParameter("activeCode");
		UserService service = new UserService();
		service.active(activeCode);
		response.sendRedirect(request.getContextPath()+"/login.jsp");
	}
	
	
	
	protected void checkUsername(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String username = request.getParameter("username");
		UserService service = new UserService();
		boolean isExist = service.checkUsername(username);
		
		String json = "{\"isExist\":"+ isExist +"}";
		response.getWriter().write(json);
	}
	
	
	protected void register(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
		String code= CommonsUtils.getUUID();
		user.setCode(code);
		
		UserService service = new UserService();
		
		boolean isRegisterSuccess = service.regist(user);
		
		if(isRegisterSuccess){
			//发送邮件
			String emailMsg = "恭喜您注册成功，请点击下面的链接进行激活"
					+"<a href='http://localhost:8081/simpleshop/user?method=active&activeCode="+code+"'>"
					+"http://localhost:8081/simpleshop/user?method=active&activeCode="+code+"<a/>"; 
			
			try {
				MailUtils.sendMail(user.getEmail(), emailMsg);
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			response.sendRedirect(request.getContextPath()+"/registerSuccess.jsp");
		}else{
			response.sendRedirect(request.getContextPath()+"/registerFail.jsp");
		}
	}
}
