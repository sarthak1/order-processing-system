package com.peerislands.orderprocessing.scheduler;

import com.peerislands.orderprocessing.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStatusScheduler {

    private final OrderService orderService;

    // Runs every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void updatePendingOrders() {
        orderService.updateOrderStatusToProcessing();
    }
}
