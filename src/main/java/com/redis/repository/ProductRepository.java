package com.redis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.redis.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
