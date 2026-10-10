package org.gallegos.entity;

import java.time.LocalDateTime;

import org.gallegos.enums.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "orders")
@Data
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idOrder;
	
	@Column(nullable = false, unique = true)
	private String numOrder;
	
	@Column(nullable = false)
	private Integer idProduct;
	
	@Column(nullable = false)
	private String nameProduct;
	
	@Column(nullable = false)
	private String nameClient;
	
	@Column(nullable = false)
	private Double unitPrice;
	
	@Column(nullable = false)
	private Double total;
	
	@Column(nullable = false)
	private Integer quantity;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus orderStatus;
	
	@Column(nullable = false)
	private LocalDateTime timeRegister;

}
