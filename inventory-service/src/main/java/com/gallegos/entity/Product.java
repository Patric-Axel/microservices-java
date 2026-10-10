package com.gallegos.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idProduct;
	
	@Column(nullable = false, unique = true)
	private String sku;
	
	@Column(nullable = false)
	private String nameProduct;
	
	@Column(nullable = false)
	private Double price;
	
	@Column(nullable = false)
	private Integer stock;
	
	@Column(nullable = false)
	private LocalDateTime timeRegister;
	
}
