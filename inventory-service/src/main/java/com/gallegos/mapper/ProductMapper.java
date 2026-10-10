package com.gallegos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.gallegos.dto.ProductRequest;
import com.gallegos.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

	@Mapping(target = "idProduct", ignore = true)
	@Mapping(target = "timeRegister", ignore = true)
	Product toEntity(ProductRequest request);
}
