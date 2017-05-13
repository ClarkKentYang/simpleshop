package com.simpleshop.web.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;

import com.google.gson.Gson;
import com.simpleshop.domain.Cart;
import com.simpleshop.domain.CartItem;
import com.simpleshop.domain.Category;
import com.simpleshop.domain.Order;
import com.simpleshop.domain.OrderItem;
import com.simpleshop.domain.PageBean;
import com.simpleshop.domain.Product;
import com.simpleshop.domain.User;
import com.simpleshop.service.ProductService;
import com.simpleshop.utils.CommonsUtils;
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
		case "addProductToCart":
			addProductToCart(request,response);
		break;
		case "delProFromCart":
			delProFromCart(request,response);
		break;	
		case "clearCart":
			clearCart(request,response);
		break;
		case "submitOrder":
			submitOrder(request,response);
		break;
		case "confirmOrder":
			confirmOrder(request,response);
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

	
	//商品添加至购物车
	protected void addProductToCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		
		//获取购物车
		HttpSession session = request.getSession();
		
		ProductService productService = new ProductService();
		
		String pid = request.getParameter("pid");
		int buyNum = Integer.parseInt(request.getParameter("buyNum"));
		
		Product product = productService.findProductByPid(pid);
		double subtotal = product.getShop_price() * buyNum;//小计
		
		CartItem item = new CartItem();
		item.setBuyNum(buyNum);
		item.setProduct(product);
		item.setSubtotal(subtotal);
		
		Cart cart = (Cart)session.getAttribute("cart");
		if(cart==null){
			cart = new Cart();
		}
		Map<String,CartItem> cartItems = cart.getCartItems();
		
		double newsubtotal = 0.0;
		
		if(cartItems.containsKey(pid)){
			//修改购买数量
			CartItem cartItem = cartItems.get(pid);
			int oldBuyNum = cartItem.getBuyNum();
			oldBuyNum+=buyNum;
			cartItem.setBuyNum(oldBuyNum);
			cart.setCartItems(cartItems);
			//修改小计
			double oldsubtotal = cartItem.getSubtotal();
			newsubtotal = buyNum*product.getShop_price();
			cartItem.setSubtotal(oldsubtotal+newsubtotal);
			
		}else{
			cart.getCartItems().put(pid, item);
			newsubtotal = buyNum*product.getShop_price();
		}
		
		double total = cart.getTotal() + newsubtotal;
		cart.setTotal(total); 
		session.setAttribute("cart", cart);
		
		//跳转到购物车
		//request.getRequestDispatcher("/cart.jsp").forward(request, response);
		response.sendRedirect(request.getContextPath()+"/cart.jsp");
	} 

	
	//删除购物车的商品
	protected void delProFromCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String pid = request.getParameter("pid");
		HttpSession session = request.getSession();
		Cart cart = (Cart) session.getAttribute("cart");
		if(cart!=null){
			Map<String,CartItem> cartItems = cart.getCartItems();
			//修改总价
			cart.setTotal(cart.getTotal()-cartItems.get(pid).getSubtotal());
			cartItems.remove(pid);
			cart.setCartItems(cartItems);
			
		}
		session.setAttribute("cart", cart);
		response.sendRedirect(request.getContextPath()+"/cart.jsp");
	}
	
	//清空购物车
	protected void clearCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		HttpSession session = request.getSession();
		session.removeAttribute("cart");
		response.sendRedirect(request.getContextPath()+"/cart.jsp");
	}
	
	//提交订单
	protected void submitOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		Cart cart = (Cart) session.getAttribute("cart");
		if(user==null){
			response.sendRedirect(request.getContextPath()+"/login.jsp");
			return;
		}
		
		Order order = new Order();
		order.setOid(CommonsUtils.getUUID());
		order.setOrdertime(new Date());
		order.setTotal(cart.getTotal());
		order.setState(0);
		order.setUser(user);
		
		Map<String,CartItem> cartItems = cart.getCartItems();
		for (Entry<String, CartItem> entry : cartItems.entrySet()) {
			OrderItem item = new OrderItem();
			item.setItemid(CommonsUtils.getUUID());
			item.setCount(entry.getValue().getBuyNum());
			item.setSubtotal(entry.getValue().getSubtotal());
			item.setProduct(entry.getValue().getProduct());
			item.setOrder(order);
			
			order.getOrderItems().add(item);
		}
		
		ProductService service = new ProductService();
		try {
			service.submitOrder(order);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		session.setAttribute("order", order);
		response.sendRedirect(request.getContextPath()+"/order_info.jsp");
	}
	
	//确认订单
	protected void confirmOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		//更新收货人
		Map<String, String[]> properties = request.getParameterMap();
		Order order = new Order();
		try {
			BeanUtils.populate(order, properties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ProductService service = new ProductService();
		service.updateOrderAdrr(order);
		
		//在线支付
	}
}
