package com.gallegos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.gallegos.dto.ProductRequest;
import com.gallegos.entity.Product;
import com.gallegos.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService pService;

	@GetMapping
	public ResponseEntity<List<Product>> listAllProducts() {
		List<Product> lstProducts = pService.listAllProduct();
		return ResponseEntity.ok(lstProducts);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
		return ResponseEntity.ok(pService.getProductById(id));
	}

	@PostMapping
	public ResponseEntity<Product> addProduct(@Valid @RequestBody ProductRequest request) {
		Product p = pService.addProduct(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(p);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<Product> updateProduct(@PathVariable Integer id, @RequestBody Product product) {
		return ResponseEntity.ok(pService.updateProduct(id, product));
	}

	// SERA CONSUMIDO PARA VALIDAR POR CONFIRMAR ORDEN
	@PatchMapping("/{id}/stock")
	public ResponseEntity<Boolean> decreaseStock(@PathVariable Integer id, @RequestParam Integer quantity) {
		Boolean result = pService.decreaseStock(id, quantity);

		return ResponseEntity.ok(result);
	}

	// SERA CONSUMIDO POR CANCELAR ORDEN
	@PatchMapping("/{id}/stock/restore")
	public ResponseEntity<Boolean> increaseStock(@PathVariable Integer id, @RequestParam Integer quantity) {
		Boolean updated = pService.increaseStock(id, quantity);
		return ResponseEntity.ok(updated);
	}

}
