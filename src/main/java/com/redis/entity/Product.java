package com.redis.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

//@Entity
public class Product {
	@Id

	//@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String name;
	private double price;

	public Product(long l, String name, double price) {
		super();
		this.id = l;
		this.name = name;
		this.price = price;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

}
