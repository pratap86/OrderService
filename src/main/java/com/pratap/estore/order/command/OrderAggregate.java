package com.pratap.estore.order.command;

import com.pratap.estore.order.command.commands.CreateOrderCommand;
import com.pratap.estore.order.core.events.OrderCreatedEvent;
import com.pratap.estore.order.core.model.constant.OrderStatus;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

/**
 * Handle the CreateOrderCommand and publish the OrderCreatedEvent
 * @author Pratap Narayan
 */
@Slf4j
@NoArgsConstructor
@Aggregate
public class OrderAggregate {

    @AggregateIdentifier
    private String orderId;
    private String productId;
    private String userId;
    private int quantity;
    private String addressId;
    private OrderStatus orderStatus;

    @CommandHandler
    public OrderAggregate(CreateOrderCommand createOrderCommand){

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        BeanUtils.copyProperties(createOrderCommand, orderCreatedEvent);

        log.info("Publishing the orderCreatedEvent={}", orderCreatedEvent);
        AggregateLifecycle.apply(orderCreatedEvent);
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent orderCreatedEvent) throws Exception {

        log.info("setting the properties from orderCreatedEvent={}", orderCreatedEvent);
        this.orderId = orderCreatedEvent.getOrderId();
        this.productId = orderCreatedEvent.getProductId();
        this.userId = orderCreatedEvent.getUserId();
        this.addressId = orderCreatedEvent.getAddressId();
        this.quantity = orderCreatedEvent.getQuantity();
        this.orderStatus = orderCreatedEvent.getOrderStatus();
    }
}
