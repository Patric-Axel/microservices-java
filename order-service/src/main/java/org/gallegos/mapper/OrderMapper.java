package org.gallegos.mapper;

import org.gallegos.dto.OrderRequest;
import org.gallegos.dto.ProductDTO;
import org.gallegos.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

	@Mapping(source = "request.idProduct", target = "idProduct")
    @Mapping(source = "request.quantity", target = "quantity")
    @Mapping(source = "request.nameClient", target = "nameClient")
    @Mapping(source = "product.nameProduct", target = "nameProduct")
    @Mapping(source = "product.price", target = "unitPrice")

    @Mapping(target = "idOrder", ignore = true)
    @Mapping(target = "numOrder", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "orderStatus", ignore = true)
    @Mapping(target = "timeRegister", ignore = true)
	
    Order toEntity(OrderRequest request, ProductDTO product);
}
