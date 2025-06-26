package com.peerislands.orderprocessing.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private Order order;

    private String productName;
    private BigDecimal price;
    private int quantity;
    private BigDecimal total;
}
