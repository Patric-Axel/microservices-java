package org.gallegos.mapper;

import org.gallegos.entity.Order;
import org.gallegos.entity.OrderHistory;
import org.gallegos.enums.OrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderHistoryMapper {

	@Mapping(target = "idHistory", ignore = true)
    @Mapping(source = "order", target = "order")
    @Mapping(source = "status", target = "orderStatus")
    @Mapping(target = "changeDate", expression = "java(java.time.LocalDateTime.now())")
	
    OrderHistory toEntity(Order order, OrderStatus status);
}
