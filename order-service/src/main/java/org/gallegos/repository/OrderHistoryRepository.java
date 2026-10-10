package org.gallegos.repository;

import java.util.List;

import org.gallegos.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Integer>{

	List<OrderHistory> findByOrderIdOrder(Integer idOrder);
}
