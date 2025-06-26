package com.peerislands.orderprocessing.repository;

import com.peerislands.orderprocessing.model.Order;
import com.peerislands.orderprocessing.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items")
    List<Order> findAllWithItems();
}
