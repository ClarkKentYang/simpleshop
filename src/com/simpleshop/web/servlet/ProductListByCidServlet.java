package com.simpleshop.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.simpleshop.domain.PageBean;
import com.simpleshop.domain.Product;
import com.simpleshop.service.ProductService;

/**
 * Servlet implementation class ProductListByCidServlet
 */
public class ProductListByCidServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public ProductListByCidServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
