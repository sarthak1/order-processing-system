package com.peerislands.orderprocessing.service;

import com.peerislands.orderprocessing.model.Order;
import com.peerislands.orderprocessing.model.OrderItem;
import com.peerislands.orderprocessing.model.OrderStatus;
import com.peerislands.orderprocessing.repository.OrderRepository;
import com.peerislands.orderprocessing.repository.OrderItemRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public Order createOrder(String customerId, String customerName, List<OrderItem> items) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setCustomerName(customerName);
        order.setStatus(OrderStatus.PENDING);
        
        order = orderRepository.save(order); // Save the order first to get its ID
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : items) {
            item.setOrder(order);
            orderItemRepository.save(item); // Save each item
            totalAmount = totalAmount.add(item.getTotal());
        }
        
        order.setItems(items);
        order.setTotalAmount(totalAmount);
        return orderRepository.save(order); // Save the order with items and total
    }

    public Order getOrderById(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    @Transactional
    public List<Order> getAllOrders() {
        return orderRepository.findAllWithItems();
    }

    @Transactional
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        
        System.out.println("Attempting to cancel order with ID: " + orderId);
        
        try {
            // First check if order exists
            Order order = orderRepository.findById(orderId).orElseThrow(
                () -> {
                    System.out.println("Order not found with ID: " + orderId);
                    return new RuntimeException("Order not found with id: " + orderId);
                }
            );
            
            System.out.println("Found order with status: " + order.getStatus());
            
            // Check order status
            if (order.getStatus() != OrderStatus.PENDING) {
                String errorMsg = String.format("Order %d is in %s status and cannot be cancelled", 
                    orderId, order.getStatus());
                System.out.println(errorMsg);
                throw new IllegalStateException(errorMsg);
            }
            
            // Update status
            order.setStatus(OrderStatus.CANCELLED);
            System.out.println("Updating order status to CANCELLED");
            
            // Save the updated order
            Order savedOrder = orderRepository.save(order);
            System.out.println("Successfully cancelled order " + orderId);
            
            return savedOrder;
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Re-throw validation/state exceptions as they are
            System.err.println("Validation error cancelling order: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            // Log other unexpected errors
            System.err.println("Unexpected error cancelling order " + orderId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to cancel order: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void updateOrderStatusToProcessing() {
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        for (Order order : pendingOrders) {
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
        }
    }
}
