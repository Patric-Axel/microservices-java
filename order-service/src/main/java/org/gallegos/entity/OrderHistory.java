package org.gallegos.entity;

import java.time.LocalDateTime;

import org.gallegos.enums.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class OrderHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idHistory;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus orderStatus;
	
	@Column(nullable = false)
	private	LocalDateTime changeDate;
	
	@ManyToOne
	@JoinColumn(name = "id_order", nullable = false)
	private Order order;
	
}
