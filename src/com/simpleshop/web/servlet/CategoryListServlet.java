package com.simpleshop.web.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.simpleshop.domain.Category;
import com.simpleshop.service.ProductService;
import com.simpleshop.utils.JedisPoolUtils;

import redis.clients.jedis.Jedis;

/**
 * Servlet implementation class CategoryListServlet
 */
public class CategoryListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public CategoryListServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ProductService service = new ProductService();
		List<Category> categoryList = null;
		String json = null;
		
		//先从内存中查询，如果有直接使用，如果没有从数据库中读取，存到缓存
		Jedis jedis = JedisPoolUtils.getJedis();
		String categoryListJson = jedis.get("categoryListJson");
		//categoryListJson需要判断是否为null
		if(categoryListJson==null){
			System.out.println("缓存中没有数据，从数据库中查询");
			categoryList = service.fineCategroyList();
			Gson gson = new Gson();
			json = gson.toJson(categoryList);
			jedis.set("categoryListJson",json);
		}else{
			json = categoryListJson;
		}
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(json);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
