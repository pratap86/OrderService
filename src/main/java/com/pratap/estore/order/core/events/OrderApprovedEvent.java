package com.pratap.estore.order.core.events;

import com.pratap.estore.order.core.model.constant.OrderStatus;
import lombok.Value;

@Value
public class OrderApprovedEvent {

    private final String orderId;
    private final OrderStatus orderStatus = OrderStatus.APPROVED;
}
