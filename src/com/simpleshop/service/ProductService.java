package com.simpleshop.service;

import java.sql.SQLException;
import java.util.List;

import com.simpleshop.dao.ProductDao;
import com.simpleshop.domain.Category;
import com.simpleshop.domain.Order;
import com.simpleshop.domain.PageBean;
import com.simpleshop.domain.Product;
import com.simpleshop.utils.DataSourceUtils;
import com.sun.org.apache.regexp.internal.recompile;

public class ProductService {

	public List<Product> findHotProductList() {
		ProductDao dao = new ProductDao();
		List<Product> hotList = null;
		try {
			hotList =  dao.findHotProductList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hotList;
	}

	public List<Product> findNewProductList() {
		ProductDao dao = new ProductDao();
		List<Product> newList = null;
		try {
			newList =  dao.findNewProductList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newList;
	}

	public List<Category> fineCategroyList() {
		ProductDao dao = new ProductDao();
		List<Category> categoryList = null;
		try {
			categoryList =  dao.findCategroyList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categoryList;
	}

	public PageBean<Product> findProductListByCid(String cid,int currentPage,int currentCount) {
		
		ProductDao dao = new ProductDao();
		
		PageBean<Product> pageBean = new PageBean<>();
		List<Product> list = null;

		
		int totalCount = 0;
		try {
			totalCount = dao.getCount(cid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		pageBean.setCurrentCount(currentCount);
		pageBean.setCurrentPage(currentPage);
		pageBean.setTotalCount(totalCount);
		pageBean.setTotalPage((int) Math.ceil(1.0*totalCount/currentCount));
		
		try {
			list = dao.findProductByPage(cid,(currentPage-1)*currentCount,currentCount);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pageBean.setList(list);
		
		return pageBean;
	}

	public Product findProductByPid(String pid) {
		ProductDao productDao = new ProductDao();
		Product product = null;
		try {
			product = productDao.findProductByPid(pid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return product;
	}

	public void submitOrder(Order order) throws SQLException {
		ProductDao dao = new ProductDao();
		try {
			//开启事务
			DataSourceUtils.startTransaction();
			dao.addOrders(order);
			dao.addOrderItem(order);
		} catch (SQLException e) {
			DataSourceUtils.rollback();
			e.printStackTrace();
		} finally {
			DataSourceUtils.commitAndRelease();
		}
	}

	public void updateOrderAdrr(Order order) {
		ProductDao dao = new ProductDao();
		try {
			dao.updateOrderAdrr(order);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
