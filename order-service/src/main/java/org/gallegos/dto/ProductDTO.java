package org.gallegos.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProductDTO {

	private Integer idProduct;
	private String sku;
	private String nameProduct;
	private Double price;
	private Integer stock;
	private LocalDateTime timeRegister;
}
