package com.redis.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.redis.entity.Product;

@Service
public class ProductService {

	// Simulating a database with a map
	private static final Map<Long, Product> PRODUCT_DB = new HashMap();
	private static List<Product> list = new ArrayList();

	static {
		PRODUCT_DB.put(1L, new Product(1L, "Laptop", 999.99));
		PRODUCT_DB.put(2L, new Product(2L, "Smartphone", 499.99));
		PRODUCT_DB.put(3L, new Product(3L, "Tablet", 299.99));
		try {
			list.add(PRODUCT_DB.get(1L));
			list.add(PRODUCT_DB.get(2L));
			list.add(PRODUCT_DB.get(3L));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	public void addProduct(Product product) {
		Product prod = new Product(product.getId(), product.getName(), product.getPrice());
		list.add(prod);
	}

	//// Caching a list of all products
	
	@Cacheable(value = "products")
	public List<Product> getProductDetails() {
		return list;
	}
//// Caching a single product by ID
	@Cacheable(value = "products", key = "#id")
	public Product getProductDetailsbyid(long id) {
		System.out.println("Fetching product from database for ID: " + id);
		simulateSlowService(); // Simulate a slow DB call
		return PRODUCT_DB.get(id);

	}
	
	 // Update product and refresh the cache for the single product

	@CachePut(value = "products",key = "#product.id")
	/*
	 * This ensures that after a product is updated, the entire cached list is
	 * evicted so that the next request will fetch the updated list from the
	 * database.
	 */
	@CacheEvict(value = "products", allEntries = true)  // Invalidate the cached list

	public Product updateProductDetails(Product product) {
		System.out.println("Updating product in database for ID: " + product.getId());
		PRODUCT_DB.put(product.getId(), product);
		return product;
	}
	// Delete a product by ID and evict it from the cache
	@CacheEvict(value = "products",key = "#id")
	public void deleteProductDetails(long id) {
		System.out.println("Deleting product from database for ID: " + id);
		PRODUCT_DB.remove(id);
	}

	
	 // Simulate a slow service call (to mimic database delay)
	private void simulateSlowService() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
