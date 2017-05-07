package com.simpleshop.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.simpleshop.domain.Category;
import com.simpleshop.domain.PageBean;
import com.simpleshop.domain.Product;
import com.simpleshop.service.ProductService;
import com.simpleshop.utils.JedisPoolUtils;

import redis.clients.jedis.Jedis;

/**
 * Servlet implementation class ProductServlet
 */
public class ProductServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public ProductServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
		
		String method = request.getParameter("method");
		switch (method) {
		case "categoryList":
			categoryList(request,response);
			break;
		case "index":
			index(request,response);
			break;
		case "productInfo":
			productInfo(request,response);
			break;
		case "productListByCid":
			productListByCid(request,response);
			break;

		default:
			break;
		}
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

	
	protected void categoryList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
	
	
	protected void index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ProductService service = new ProductService();
		//获取热门商品
		List<Product> hotProductList= service.findHotProductList();
		//获取最新商品
		List<Product> newProductList= service.findNewProductList();
		//分类列表
		List<Category> categoryList = service.fineCategroyList();
		
		request.setAttribute("hotProductList", hotProductList);
		request.setAttribute("newProductList", newProductList);
		request.setAttribute("categoryList", categoryList);
		
		request.getRequestDispatcher("/index.jsp").forward(request,response);
	}
	
	
	protected void productInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String currentPage = request.getParameter("currentPage");
		String cid = request.getParameter("cid");
		
		String pid = request.getParameter("pid");
		ProductService productService = new ProductService();
		Product product = productService.findProductByPid(pid);
		
		request.setAttribute("product", product);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("cid", cid);
		
		//获取客户端的cookie，pids存储历史记录
		String pids = pid;
		Cookie[] cookies = request.getCookies();
		if(cookies!=null){
			for (Cookie cookie : cookies) {
				if("pids".equals(cookie.getName())){
					pids = cookie.getValue();
					
					String[] split = pids.split("-");
					List<String> asList = Arrays.asList(split);
					LinkedList<String> list = new LinkedList<>(asList);
					//判断集合中是否存在当前pid
					if(list.contains(pid)){
						list.remove(pid);
					}
					list.addFirst(pid);
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < list.size()&&i<7; i++) {
						sb.append(list.get(i));
						sb.append("-");
					}
					pids = sb.substring(0,sb.length()-1);
				}
			}
		}
		Cookie cookie_pids = new Cookie("pids",pids);
		response.addCookie(cookie_pids);
		
		request.getRequestDispatcher("/product_info.jsp").forward(request, response);
	}
	
	
	protected void productListByCid(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String cid = request.getParameter("cid");
		
		String currentPageStr = request.getParameter("currentPage");
		
		int currentPage;
		int currentCount = 12;
		
		if(currentPageStr==null){
			currentPage = 1;
		}else{
			currentPage = Integer.parseInt(currentPageStr);
		}
		
		
		
		ProductService service = new ProductService();
		PageBean<Product> pageBean = service.findProductListByCid(cid,currentPage,currentCount);
		
		request.setAttribute("pageBean", pageBean);
		request.setAttribute("cid", cid);
		
		//定义一个存储浏览记录的list
		List<Product> historyProductList = new ArrayList<>();
		
		//获取客户端携带pid的cookie
		Cookie[] cookies = request.getCookies();
		if(cookies!=null){
			for (Cookie cookie : cookies) {
				if("pids".equals(cookie.getName())){
					String pids = cookie.getValue();
					String[] split = pids.split("-");
					for (String pid : split) {
						Product p = service.findProductByPid(pid);
						historyProductList.add(p);
					}
				}
			}
		}
		
		request.setAttribute("historyProductList", historyProductList);
		
		request.getRequestDispatcher("/product_list.jsp").forward(request, response);
	}
}
