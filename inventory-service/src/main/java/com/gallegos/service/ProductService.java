package com.gallegos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import com.gallegos.dto.ProductRequest;
import com.gallegos.entity.Product;
import com.gallegos.exception.ProductNotFoundException;
import com.gallegos.mapper.ProductMapper;
import com.gallegos.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository pRepository;
	private final ProductMapper productMapper;

	public List<Product> listAllProduct() {
		return pRepository.findAll();
	}

	public Product getProductById(Integer id) {
		return pRepository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con el Id: " + id));
	}

	public Product addProduct(ProductRequest request) {
		Product product = productMapper.toEntity(request);
		product.setTimeRegister(LocalDateTime.now());
		return pRepository.save(product);
	}

	public Product updateProduct(Integer id, Product product) {

		Product existProduct = pRepository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con el Id: " + id));

		if (product.getNameProduct() != null) {
			existProduct.setNameProduct(product.getNameProduct());
		}
		if (product.getPrice() != null) {
			existProduct.setPrice(product.getPrice());
		}
		if (product.getStock() != null) {
			existProduct.setStock(product.getStock());
		}

		return pRepository.save(existProduct);
	}

	@Transactional
	public Boolean decreaseStock(Integer idProduct, Integer quantity) {
		if (quantity == null || quantity <= 0) {
			return false;
		}

		int updatedRows = pRepository.decreaseStock(idProduct, quantity);
		return updatedRows > 0;
	}

	@Transactional
	public Boolean increaseStock(Integer idProduct, Integer quantity) {
		if (quantity == null || quantity <= 0) {
			return false;
		}

		int updatedRows = pRepository.increaseStock(idProduct, quantity);
		return updatedRows > 0;
	}

}
