package com.peerislands.orderprocessing;

import com.peerislands.orderprocessing.model.Order;
import com.peerislands.orderprocessing.model.OrderItem;
import com.peerislands.orderprocessing.model.OrderStatus;
import com.peerislands.orderprocessing.repository.OrderRepository;
import com.peerislands.orderprocessing.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Create a test order
        testOrder = new Order();
        testOrder.setCustomerId("test-customer");
        testOrder.setCustomerName("Test Customer");
        testOrder.setStatus(OrderStatus.PENDING);
        
        OrderItem item = new OrderItem();
        item.setProductName("Test Product");
        item.setPrice(new BigDecimal("19.99"));
        item.setQuantity(2);
        item.setTotal(new BigDecimal("39.98"));
        item.setOrder(testOrder);
        
        testOrder.getItems().add(item);
        testOrder.setTotalAmount(item.getTotal());
        
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    void testCancelOrder() {
        // Given - test order is created in setup
        
        // When
        Order cancelledOrder = orderService.cancelOrder(testOrder.getId());
        
        // Then
        assertNotNull(cancelledOrder);
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
        
        // Verify the order is still in the database
        Order foundOrder = orderRepository.findById(testOrder.getId()).orElse(null);
        assertNotNull(foundOrder);
        assertEquals(OrderStatus.CANCELLED, foundOrder.getStatus());
    }

    @Test
    void testCancelOrder_NotFound() {
        try {
            orderService.cancelOrder(9999L);
            fail("Expected exception not thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("not found"));
        }
    }

    @Test
    void testCancelOrder_NotPending() {
        // Given - update order status to SHIPPED
        testOrder.setStatus(OrderStatus.SHIPPED);
        testOrder = orderRepository.save(testOrder);
        
        // When/Then
        try {
            orderService.cancelOrder(testOrder.getId());
            fail("Expected exception not thrown");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("cannot be cancelled"));
        }
    }
}
