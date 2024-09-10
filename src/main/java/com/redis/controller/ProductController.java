package com.redis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.redis.entity.Product;
import com.redis.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;

import java.util.List;

@RestController
public class ProductController {

	@Autowired(required = true)
	ProductService productService;

	@PostMapping(path = "/product")
	public ResponseEntity<Product> createProduct(@RequestBody Product product) {

		productService.addProduct(product);

		return new ResponseEntity<>(HttpStatus.CREATED);

	}

	@GetMapping(path = "/product")
	public ResponseEntity<List<Product>> getProduct() {

		List<Product> list = productService.getProductDetails();
		return new ResponseEntity<List<Product>>(list, HttpStatus.OK);
	}

}
