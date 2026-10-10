package org.gallegos.controller;

import java.util.List;

import org.gallegos.dto.OrderRequest;
import org.gallegos.entity.Order;
import org.gallegos.entity.OrderHistory;
import org.gallegos.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@GetMapping
	public ResponseEntity<List<Order>> allOrderList() {
		List<Order> lstOrders = orderService.listAllOrders();
		return ResponseEntity.ok(lstOrders);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Order> getOrderById(@PathVariable Integer id) {
		return ResponseEntity.ok(orderService.getOrderById(id));
	}

	@GetMapping("/{id}/history")
	public ResponseEntity<List<OrderHistory>> getOrderHistory(@PathVariable Integer id) {
		return ResponseEntity.ok(orderService.getHistoryByOrder(id));
	}

	@PostMapping
	public ResponseEntity<Order> registerOrder(@Valid @RequestBody OrderRequest request) {
		Order order = orderService.registerOrder(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(order);
	}

	@PatchMapping("/{id}/cancel")
	public ResponseEntity<Order> cancelOrder(@PathVariable Integer id) {
		Order order = orderService.cancelOrder(id);
		return ResponseEntity.ok(order);
	}

}
