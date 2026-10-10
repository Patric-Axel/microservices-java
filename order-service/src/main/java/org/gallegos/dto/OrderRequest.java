package org.gallegos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderRequest {

	@NotNull(message = "Ingresar id producto")
	private Integer idProduct;
	
	@NotNull(message = "Ingresar cantidad")
	@Positive(message = "positivo y mayor a 0")
	private Integer quantity;
	
	@NotNull(message = "Ingresar nombre del cliente")
	private String nameClient;
	
	
}
