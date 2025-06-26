package com.peerislands.orderprocessing.controller;

import com.peerislands.orderprocessing.model.Order;
import com.peerislands.orderprocessing.model.OrderItem;
import com.peerislands.orderprocessing.model.OrderStatus;
import com.peerislands.orderprocessing.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    private static final String ALL_ORDERS_EXAMPLE = "[{\"id\":1,\"customerId\":\"123\",\"customerName\":\"John Doe\",\"status\":\"PENDING\",\"totalAmount\":200.00}," +
            "{\"id\":2,\"customerId\":\"456\",\"customerName\":\"Jane Smith\",\"status\":\"DELIVERED\",\"totalAmount\":150.00}]";

    private static final String CREATE_ORDER_EXAMPLE = "{\"id\":1,\"customerId\":\"123\",\"customerName\":\"John Doe\",\"status\":\"PENDING\",\"totalAmount\":200.00,\"createdAt\":\"2025-06-26T13:38:33+05:30\",\"updatedAt\":\"2025-06-26T13:38:33+05:30\",\"items\":[{\"id\":1,\"productName\":\"Test Product\",\"price\":100.00,\"quantity\":2,\"total\":200.00,\"order_id\":1}]}";

    @Operation(summary = "Create a new order")
    @ApiResponse(responseCode = "200", description = "Order created successfully", 
        content = @Content(schema = @Schema(implementation = Order.class), 
            examples = {
                @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Created Order Response",
                    value = CREATE_ORDER_EXAMPLE)
            }))
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        // Convert request items to OrderItems
        List<OrderItem> orderItems = request.getItems().stream()
            .map(itemRequest -> {
                OrderItem item = new OrderItem();
                item.setProductName(itemRequest.getProductName());
                item.setPrice(itemRequest.getPrice());
                item.setQuantity(itemRequest.getQuantity());
                item.setTotal(itemRequest.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
                return item;
            })
            .collect(Collectors.toList());
        
        // Create order and save it
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setCustomerName(request.getCustomerName());
        order.setStatus(OrderStatus.PENDING);
        order.setItems(orderItems);
        
        // Calculate total amount
        BigDecimal totalAmount = orderItems.stream()
            .map(OrderItem::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        
        // Save order
        Order savedOrder = orderService.createOrder(order.getCustomerId(), 
            order.getCustomerName(), orderItems);
            
        return ResponseEntity.ok(savedOrder);
    }

    @Operation(summary = "Get order by ID")
    @ApiResponse(responseCode = "200", description = "Order found", 
        content = @Content(schema = @Schema(implementation = Order.class)))
    @ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(
        @Parameter(description = "ID of the order to retrieve", required = true, example = "1")
        @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(summary = "Get all orders")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully", 
        content = @Content(schema = @Schema(implementation = Order.class), 
            examples = {
                @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Example Response",
                    value = ALL_ORDERS_EXAMPLE)
            }))
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @Operation(summary = "Get orders by status")
    @ApiResponse(responseCode = "200", description = "Orders found", 
        content = @Content(schema = @Schema(implementation = Order.class)))
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(
        @Parameter(description = "Status to filter orders by. Must be one of: PENDING, PROCESSING, COMPLETED, CANCELLED", 
                   required = true,
                   example = "PENDING")
        @PathVariable String status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(OrderStatus.valueOf(status.toUpperCase())));
    }

    @Operation(
        summary = "Cancel an order",
        parameters = {
            @Parameter(
                name = "id",
                in = ParameterIn.PATH,
                description = "ID of the order to cancel. Must be a valid order ID that exists in the system.",
                required = true,
                example = "1"
            )
        }
    )
    @ApiResponse(responseCode = "200", description = "Order cancelled successfully", 
        content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "400", description = "Order cannot be cancelled (not in PENDING status)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable Long id) {
        try {
            Order order = orderService.cancelOrder(id);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Order cancelled successfully");
            response.put("orderId", String.valueOf(id));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Log the error
            System.err.println("Error cancelling order " + id + ": " + e.getMessage());
            e.printStackTrace();
            
            // Return appropriate error response
            Map<String, String> errorResponse = new HashMap<>();
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                errorResponse.put("error", "Order not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            } else if (e.getMessage() != null && e.getMessage().contains("not in PENDING status")) {
                errorResponse.put("error", "Order cannot be cancelled as it's not in PENDING status");
                return ResponseEntity.badRequest().body(errorResponse);
            } else {
                errorResponse.put("error", "An error occurred while cancelling the order: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
    }
}

class CreateOrderRequest {
    private String customerId;
    private String customerName;
    private List<OrderItemRequest> items;

    // Getters and setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}

@Schema(description = "Order item request")
class OrderItemRequest {
    private String productName;
    private BigDecimal price;
    private int quantity;

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public OrderItem toOrderItem() {
        OrderItem item = new OrderItem();
        item.setProductName(productName);
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setTotal(price.multiply(BigDecimal.valueOf(quantity)));
        return item;
    }
}
