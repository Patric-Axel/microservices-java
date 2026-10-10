package com.gallegos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gallegos.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

	@Modifying
	@Query("""
			UPDATE Product p
			SET p.stock = p.stock - :quantity
			WHERE p.idProduct = :idProduct
			AND p.stock >= :quantity
			""")
	int decreaseStock(
			@Param("idProduct") Integer idProduct,
			@Param("quantity") Integer quantity);

	@Modifying
	@Query("""
	    UPDATE Product p
	    SET p.stock = p.stock + :quantity
	    WHERE p.idProduct = :idProduct
	""")
	int increaseStock(
	        @Param("idProduct") Integer idProduct,
	        @Param("quantity") Integer quantity
	);
}
