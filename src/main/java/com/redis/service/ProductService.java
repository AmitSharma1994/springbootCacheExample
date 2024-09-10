package com.redis.service;

import java.util.ArrayList;

import java.util.List;

import org.springframework.stereotype.Service;

import com.redis.entity.Product;

@Service
public class ProductService {
	
	List<Product> list=new ArrayList();
	
	
	public void addProduct(Product product) {
		Product prod=new Product(product.getId(), product.getName(), product.getPrice());
		list.add(prod);
	}
	
	public List<Product> getProductDetails(){
		return list;
	}

}
