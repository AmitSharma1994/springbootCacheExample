package com.redis.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.redis.entity.Product;
import com.redis.repository.ProductRepository;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	public Product addProduct(Product product) {
		return productRepository.save(product);
	}

	//// Caching a list of all products
	@Cacheable(value = "products-all")
	public List<Product> getProductDetails() {
		System.out.println(">>> Cache MISS — fetching all products from H2");
		return productRepository.findAll();
	}

	//// Caching a single product by ID
	@Cacheable(cacheResolver = "shardCacheResolver", key = "#id")
	public Product getProductDetailsbyid(long id) {
		System.out.println(">>> Cache MISS — hitting H2 for ID: " + id);
		simulateSlowService();
		return productRepository.findById(id).orElse(null);
	}

	// Update product and refresh the cache for the single product

	@CachePut(cacheResolver = "shardCacheResolver", key = "#product.id")
	/*
	 * This ensures that after a product is updated, the entire cached list is
	 * evicted so that the next request will fetch the updated list from the
	 * database.
	 */
	@CacheEvict(value = "products-all", allEntries = true) // Invalidate the cached list

	public Product updateProductDetails(Product product) {
		System.out.println("Updating product in database for ID: " + product.getId());
		return productRepository.save(product);

	}

	// Delete a product by ID and evict it from the cache
	@CacheEvict(cacheResolver = "shardCacheResolver", key = "#id")
	public void deleteProductDetails(long id) {
		System.out.println("Deleting product from database for ID: " + id);
		productRepository.deleteById(id);
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
