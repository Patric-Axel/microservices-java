package org.gallegos.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.gallegos.client.InventoryClient;
import org.gallegos.dto.OrderRequest;
import org.gallegos.dto.ProductDTO;
import org.gallegos.entity.Order;
import org.gallegos.entity.OrderHistory;
import org.gallegos.enums.OrderStatus;
import org.gallegos.exception.InvalidOrderStatusException;
import org.gallegos.exception.OrderNotFoundException;
import org.gallegos.exception.StockInsufficienteException;
import org.gallegos.mapper.OrderHistoryMapper;
import org.gallegos.mapper.OrderMapper;
import org.gallegos.repository.OrderHistoryRepository;
import org.gallegos.repository.OrderRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderHistoryRepository historyRepository;
	private final InventoryClient inventoryClient;
	private final OrderMapper orderMapper;
	private final OrderHistoryMapper historyMapper;

	public List<Order> listAllOrders() {
		return orderRepository.findAll();
	}

	public List<OrderHistory> getHistoryByOrder(Integer idOrder) {
		if (!orderRepository.existsById(idOrder)) {
			throw new OrderNotFoundException("No existe una orden con ese historial");
		}
		return historyRepository.findByOrderIdOrder(idOrder);
	}

	public Order getOrderById(Integer id) {
		return orderRepository.findById(id)
				.orElseThrow(() -> new OrderNotFoundException("La orden inexistente"));
	}

	public Order registerOrder(OrderRequest request) {
		ProductDTO product = inventoryClient.getProductById(request.getIdProduct());
		Order order = orderMapper.toEntity(request, product);

		order.setNumOrder("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		order.setTotal(product.getPrice() * request.getQuantity());
		order.setOrderStatus(OrderStatus.PENDING);
		order.setTimeRegister(LocalDateTime.now());
		
		// CAPTURAMOS EL ESTADO DE PEDIDO COMO PENDIENTE
		// PARA TENER VISIBILIDAD DEL CICLO DE VIDA
		order = orderRepository.save(order);
		historyRepository.save(historyMapper.toEntity(order, OrderStatus.PENDING));
		
		// VALIDACION DE INVENTARIO Y DESCUENTO DE STOCK SI APLICA
		Boolean stockUpdate = inventoryClient.decreaseStock(
				request.getIdProduct(), 
				request.getQuantity());
		
		// NO HAY STOCK PERMANECE PENDIENTE y CANCELAMOS
		if (!stockUpdate) {
			order.setOrderStatus(OrderStatus.CANCELLED);
			order = orderRepository.save(order);

			historyRepository.save(historyMapper.toEntity(order, OrderStatus.CANCELLED));
			throw new StockInsufficienteException("no hay stock disponible");
		}
		// SI HAY STOCK CAMBIA CONFIRMADO
		order.setOrderStatus(OrderStatus.CONFIRMED);
		order = orderRepository.save(order);
		
		// GUARDAMOS EN EL HISTORIAL
		historyRepository.save(historyMapper.toEntity(order, OrderStatus.CONFIRMED));
		return order;

	}

	public Order cancelOrder(Integer id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new OrderNotFoundException("La orden no existe"));
		
		if (order.getOrderStatus() == OrderStatus.CANCELLED) {
			throw new InvalidOrderStatusException("La orden ya está cancelada");
		}

		//SI LA ORDEN A CANCELAR ESTA CONFIRMADA DEVOLVEMOS EL STOCK 
		if (order.getOrderStatus() == OrderStatus.CONFIRMED) {

			Boolean stockRestored = inventoryClient.increaseStock(order.getIdProduct(), 
					order.getQuantity());

			if (!stockRestored) {
				throw new InvalidOrderStatusException("No se pudo restaurar el stock del producto");
			}
		}

		//CANCELAMOS LA ORDEN
		order.setOrderStatus(OrderStatus.CANCELLED);
		order = orderRepository.save(order);
		historyRepository.save(historyMapper.toEntity(order, OrderStatus.CANCELLED));

		return order;
	}

}
